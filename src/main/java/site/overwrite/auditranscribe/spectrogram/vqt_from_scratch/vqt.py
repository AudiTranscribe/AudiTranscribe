# IMPORTS
import numpy as np  # Ideally we shouldn't rely on many methods from Numpy as they aren't available in Java
import librosa

from vqt_from_scratch.audio import *
from vqt_from_scratch.helpers import *
from vqt_from_scratch.utils import *
from vqt_from_scratch.windowing import *

# CONSTANTS
# Resampling bandwidths as percentage of Nyquist Frequency
# (Taken from https://github.com/bmcfee/resampy/blob/0.2.1/resampy/filters.py)
BW_BEST = 0.9475937167399596
BW_FASTEST = 0.85


# CLASSES
class VQT:  # In java we can implement this as a static class
    """
    VQT Class that contains VQT methods
    
    Adapted largely from https://librosa.org/doc/main/_modules/librosa/core/constantq.html#vqt.

    Reference: Schoerkhuber, Christian, and Anssi Klapuri.
        "Constant-Q transform toolbox for music processing."
        7th Sound and Music Computing Conference, Barcelona, Spain. 2010.
    """

    def __init__(self):  # Don't need this in Java
        pass

    # Public methods
    def vqt(self, y, sr, hop_length=512, fmin=None, n_bins=168, bins_per_octave=24, gamma=None, window="hann"):  # Can be made static in Java
        # Compute number of octaves that we are processing and the number of filters
        n_octaves = self.get_num_octaves(n_bins, bins_per_octave)
        n_filters = min(bins_per_octave, n_bins)  # What is a 'filter'??? Todo: wouldn't this always be `bins_per_octave`?
        
        # Assign values to `None` kwargs
        if fmin is None:
            # Minimum frequency is C1 by default
            fmin = librosa.note_to_hz("C1")  # This function should be implemented under Misc utils; tuned to A440

        #tuning = 0  # Although librosa supports tuning estimation, we shouldn't need to estimate this
        dtype = librosa.util.dtype_r2c(y.dtype)  # Converts the 'real' datatype of `y` to a 'complex' datatype

        # Get the VQT frequency bins
        freqs = self.get_freq_bins(n_bins, bins_per_octave, fmin)

        # Get the frequencies for the top octave
        freqs_top = [0] * bins_per_octave  # In Java do smth like `double[] freqs_top = new double[bins_per_octave]`

        for bin_index in range(bins_per_octave):
            freqs_top[bin_index] = freqs[n_bins - bins_per_octave + bin_index]

        # Get highest frequency
        fmax_t = freqs_top[bins_per_octave - 1]  # Why on earth did they name this variable `fmax_t`?????     

        # Calculate the relative difference in frequency between any two consecutive bands, alpha
        alpha = compute_alpha(bins_per_octave)

        # Compute wavelet lengths
        lengths, filter_cutoff = compute_wavelet_lengths(freqs, sr, window, gamma, alpha)

        # Determine required resampling quality
        nyquist_freq = sr / 2.0  # See Nyquist–Shannon sampling theorem

        if filter_cutoff > nyquist_freq:
            raise ParameterError(
                f"Wavelet basis with max frequency {fmax_t} would exceed the Nyquist frequency {nyquist_freq}. "
                "Try reducing the number of frequency bins."
            )

        # Resample audio
        if filter_cutoff < BW_FASTEST * nyquist_freq:
            resample_type = "kaiser_fast"
        else:
            resample_type = "kaiser_best"
    
        y, sr, hop_length = self.early_downsample(y, sr, hop_length, resample_type, n_octaves, nyquist_freq, filter_cutoff)

        # Define VQT response array
        vqt_resp = []  # Has length `n_octaves`; todo: find the 'internal' shape

        # Skip this block for now
        # (Todo: update this comment; this really isn't that descriptive)
        oct_start = 0
        
        if resample_type != "kaiser_fast":
            # Get the top frequencies
            # freqs_top = freqs[-n_filters:]

            freqs_top = [0] * n_filters  # In Java do smth like `double[] freqs_top = new double[n_filters]`

            for bin_index in range(n_filters):
                freqs_top[bin_index] = freqs[n_bins - n_filters + bin_index]

            # Do the top octave before resampling to allow for fast resampling
            fft_basis, n_fft, _ = self.vqt_filter_fft(
                sr,
                freqs_top,
                1,  # Filter scale is 1
                1,  # Norm is 1
                window=window,
                gamma=gamma,
                dtype=dtype,
                alpha=alpha,
            )

            # Compute the VQT filter response and append it to the stack
            vqt_resp.append(
                self.vqt_response(y, n_fft, hop_length, fft_basis, dtype=dtype)
            )

            oct_start = 1

            resample_type = "kaiser_fast"

        # Iterate down the octaves
        my_y, my_sr, my_hop = y, sr, hop_length  # Todo: rename variables

        for i in range(oct_start, n_octaves):  # This starts from the HIGHEST frequencies and goes down
            # Get the frequencies of the current octave
            # Fixme: This may be incorrect with early downsampling
            freqs_oct = [0] * n_filters  # In Java do smth like `double[] freqs_oct = new double[n_filters]`

            for bin_index in range(n_filters):
                freqs_oct[bin_index] = freqs[n_bins - n_filters * (i + 1) + bin_index]

            # Get the FFT basis and the `n_fft` for this octave
            fft_basis, n_fft, _ = self.vqt_filter_fft(
                my_sr,
                freqs_oct,
                1,  # Filter scale is 1
                1,  # Norm is 1
                window=window,
                gamma=gamma,
                dtype=dtype,
                alpha=alpha,
            )

            # Re-scale the filters to compensate for downsampling
            fft_basis[:] *= np.sqrt(sr / my_sr)

            # Compute the VQT filter response and append to the stack
            vqtresponse = self.vqt_response(my_y, n_fft, my_hop, fft_basis, dtype=dtype)  # Possible error here

            vqt_resp.append(
                vqtresponse
            )

            #  Update variables
            if my_hop % 2 == 0:
                my_hop //= 2
                my_sr /= 2.0
                my_y = resample(
                    my_y, sr_orig=2, sr_new=1, res_type="kaiser_fast", scale=True
                )

        V = self.trim_stack(vqt_resp, n_bins, dtype)

        # Recompute lengths here because early downsampling may have changed our sampling rate
        lengths, _ = compute_wavelet_lengths(
            freqs=freqs,
            sr=sr,
            window=window,
            filter_scale=1,
            gamma_value=gamma,
            fallback_alpha=alpha,
        )

        # Scale `V` back to normal
        for i in range(V.shape[0]):
            scale_factor = np.sqrt(lengths[i])

            for j in range(V.shape[1]):
                V[i][j] /= scale_factor

        # Return VQT matrix
        return V


    # Private methods
    @staticmethod
    def get_num_octaves(n_bins, bins_per_octave):
        return int(np.ceil(float(n_bins) / bins_per_octave))  # `np.ceil` in java is `Math.ceil`


    @staticmethod
    def get_freq_bins(n_bins, bins_per_octave, fmin):
        frequencies = [0] * n_bins  # In Java do smth like `double[] frequencies = new double[n_bins]`

        for bin_index in range(n_bins):
            # Calculate the frequency of the current frequency bin
            freq = fmin * (2.0 ** (bin_index / bins_per_octave))

            # Append it to the list of frequencies
            frequencies[bin_index] = freq

        return np.array(frequencies)  # Converted to Numpy array for python consistency

    @staticmethod
    def early_downsample(y, sr, hop_length, res_type, n_octaves, nyquist, filter_cutoff):
        # Compute the number of early downsampling operations
        # (Note: all numpy operations here can be replaced by `Math` class)
        downsample_count1 = max(0, int(np.ceil(np.log2(BW_FASTEST * nyquist / filter_cutoff)) - 1) - 1)
        downsample_count2 = max(0, num_two_factors(hop_length) - n_octaves + 1)

        downsample_count = min(downsample_count1, downsample_count2)

        # Actually perform the downsampling
        if downsample_count > 0 and res_type == "kaiser_fast":
            # Compute how much to downsample by
            downsample_factor = 2 ** downsample_count

            # Check if the signal can actually be downsampled
            if y.shape[-1] < downsample_factor:
                raise ParameterError(f"Input signal length={len(y):d} is too short for {n_octaves:d}-octave CQT")

            # Downsample `hop_length` and the sample rate
            hop_length //= downsample_factor
            new_sr = sr / float(downsample_factor)

            # Downsample audio sample
            y = resample(y, sr_orig=sr, sr_new=new_sr, res_type=res_type, scale=True)
            sr = new_sr

        return y, sr, hop_length

    @staticmethod
    def vqt_filter_fft(sr, freqs, filter_scale, norm, window="hann", gamma=0.0, dtype=np.complex64, alpha=None):  # Todo: change name; this is a bad name
        """
        Generate the frequency domain variable-Q filter basis.

        Todo: update description
        """

        # Get the frequency and lengths of the wavelet basis
        basis, lengths = compute_wavelet_basis(
            freqs=freqs,
            sr=sr,
            filter_scale=filter_scale,
            norm=norm,
            pad_fft=True,
            window=window,
            gamma=gamma,
            alpha=alpha,
        )

        # Number of FFT bins is the second element of the shape of the basis
        n_fft = basis.shape[1]  # Todo: find a way to efficiently extract this in Java

        # Re-normalize bases with respect to the FFT window length
        for i in range(basis.shape[0]):
            normalisation_factor = lengths[i] / float(n_fft)

            for j in range(basis.shape[1]):
                basis[i][j] = basis[i][j] * normalisation_factor

        # FFT and retain only the non-negative frequencies
        fft_basis = np.fft.fft(basis, n=n_fft, axis=1)
        fft_basis = fft_basis[:, : (n_fft // 2) + 1]

        # Return required data
        return fft_basis, n_fft, lengths

    @staticmethod
    def vqt_response(y, n_fft, hop_length, fft_basis, dtype=None):
        """
        Compute the filter response with a target STFT hop.

        Assume `y` is single channeled.
        """

        # Compute the STFT matrix
        """
        Note:
        By default, `mode` is "constant" which means that the signal `y` is padded on BOTH SIDES with zeros until its
        length is a power of 2. The Java implementation of STFT should also use this.

        Todo:
        - Implement "ones" window
        """

        D = librosa.stft(
            y, n_fft=n_fft, hop_length=hop_length, window="ones", pad_mode="constant", dtype=dtype
        )

        # Define the output matrix
        output_flat = fft_basis.dot(D)  # This is matrix multiplication; todo: support it

        # Output
        return output_flat

    @staticmethod
    def trim_stack(cqt_resp, n_bins, dtype):  # Todo: rename `cqt_resp`
        """
        Helper function to trim and stack a collection of VQT responses.

        Assumes that `cqt_resp` is a numpy array with a shape like
            (X, Y, Z)
        This function returns a matrix with shape
            (X * Y, Z)
        where X * Y = n_bins
        """

        # Get maximum column
        # (Todo: find out why they use `min` instead of `max`)
        max_col = min(c_i.shape[-1] for c_i in cqt_resp)

        # Generate the output shape of the VQT matrix
        shape = (n_bins, max_col)

        # Generate output VQT matrix
        cqt_out = np.empty(shape, dtype=dtype, order="F")  # Todo: rename the variable

        # Copy per-octave data into output array
        end = n_bins

        for c_i in cqt_resp:  # Note: the FIRST element is that of the HIGHEST FREQUENCY bins
            # By default, take the whole octave
            n_oct = c_i.shape[-2]
            
            # If the whole octave is more than we can fit, take the highest bins from `c_i`
            if end < n_oct:  # Todo: check if this case ever occurs
                cqt_out[:end, :] = c_i[-end:, :max_col]
            else:
                cqt_out[end - n_oct : end, :] = c_i[:, :max_col]

            end -= n_oct

        return cqt_out
