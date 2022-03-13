# IMPORTS
import numpy as np
import librosa

from vqt_from_scratch.helpers import compute_alpha
from vqt_from_scratch.utils import normalize, pad_center


# FUNCTIONS
def wrappped_window(n, window_spec):
    """The wrapped window"""
    n_min, n_max = int(np.floor(n)), int(np.ceil(n))

    window = librosa.filters.get_window(window_spec, n_min)  # Use custom implementation of windows in Java

    if len(window) < n_max:  # This MAY get called only if `n` is a float
        window = np.pad(window, [(0, n_max - len(window))], mode="constant")  # Todo: implement this

    window[n_min:] = 0.0

    return window


def window_bandwidth(window):
    # This table was blatently stolen from http://librosa.org/doc/main/_modules/librosa/filters.html
    WINDOW_BANDWIDTHS = {
        "bart": 1.3334961334912805,
        "barthann": 1.4560255965133932,
        "bartlett": 1.333496133491805,
        "bkh": 2.0045975283585014,
        "black": 1.7269681554262326,
        "blackharr": 2.0045975283585014,
        "blackman": 1.7269681554262326,
        "blackmanharris": 2.0045975283585014,
        "blk": 1.7269681554262326,
        "bman": 1.7859588613860062,
        "bmn": 1.7859588613860062,
        "bohman": 1.7859588613860062,
        "box": 1.0,
        "boxcar": 1.0,
        "brt": 1.3334961334912805,
        "brthan": 1.4560255965133932,
        "bth": 1.4560255965133932,
        "cosine": 1.2337005350199792,
        "flat": 2.7762255046484143,
        "flattop": 2.7762255046484143,
        "flt": 2.7762255046484143,
        "halfcosine": 1.2337005350199792,
        "ham": 1.3629455320350348,
        "hamm": 1.3629455320350348,
        "hamming": 1.3629455320350348,      # We'll probably need this in Java
        "han": 1.50018310546875,
        "hann": 1.50018310546875,           # We'll probably need this in Java
        "hanning": 1.50018310546875,
        "nut": 1.9763500280946082,
        "nutl": 1.9763500280946082,
        "nuttall": 1.9763500280946082,
        "ones": 1.0,                        # We'll probably need this in Java
        "par": 1.9174603174603191,
        "parz": 1.9174603174603191,
        "parzen": 1.9174603174603191,
        "rect": 1.0,
        "rectangular": 1.0,
        "tri": 1.3331706523555851,
        "triang": 1.3331706523555851,
        "triangle": 1.3331706523555851,
    }

    try:
        return WINDOW_BANDWIDTHS[window]
    except KeyError:
        raise ValueError(f"Invalid indow {window}")


def compute_wavelet_lengths(freqs, sr, window, gamma_value, fallback_alpha, filter_scale=1):
    """
    Return length of each filter in a wavelet basis.

    Assumes `freqs` are all positive and in strictly ascending order.

    References:
        Glasberg, Brian R., and Brian CJ Moore. "Derivation of auditory filter shapes from notched-noise data."
        Hearing research 47.1-2 (1990): 103-138.
    """

    # Assure that `freqs` is a numpy array
    freqs = np.array(freqs)  # We don't need to do this in Java (i think)

    # Check the number of frequencies provided
    num_freqs = len(freqs)

    if num_freqs >= 2:  # We need at least 2 frequencies to infer alpha
        # Compute the log2 of the provided frequencies
        log_freqs = [0] * num_freqs  # In Java do smth like `double[] log_freqs = new double[num_freqs]`

        for i in range(num_freqs):
            log_freqs[i] = np.log2(freqs[i])  # In Java use `MathUtils.log2` in place of `np.log2`

        # Approximate the local octave resolution
        bpo = np.empty(num_freqs)  # In Java, do smth like `double[] bpo = new double[num_freqs]`

        bpo[0] = 1 / (log_freqs[1] - log_freqs[0])  # First element case
        bpo[num_freqs-1] = 1 / (log_freqs[num_freqs-1] - log_freqs[num_freqs-2])  # Last element case

        for i in range(1, num_freqs - 1):  # Intermediate elements case
            bpo[i] = 2 / (log_freqs[i + 1] - log_freqs[i - 1])

        # Calculate alphas for the each frequency bin
        alphas = [0] * num_freqs  # In Java do smth like `double[] alphas = new double[num_freqs]`

        for i in range(num_freqs):
            alphas[i] = compute_alpha(bpo[i])  # Using above method
    else:
        alphas = [fallback_alpha]  # In Java should be defined like `double[] alphas = new {fallback_alpha}` or smth

    # Compute gamma coefficients if not provided
    gammas = [0] * num_freqs  # In Java do smth like `double[] gammas = new double[num_freqs]`
        
    if gamma_value is None:
        coefficient = 24.7 / 0.108  # This special constant is from the paper

        for i in range(num_freqs):
            gammas[i] = coefficient * alphas[i]
    else:
        # Assign every element of `gammas` to the `gamma_value`
        for i in range(num_freqs):
            gammas[i] = gamma_value

    # Compute Q-Factor matrix
    Q = [0] * num_freqs  # In Java do smth like `double[] Q = new double[num_freqs]`
    
    for i in range(num_freqs):
        Q[i] = float(filter_scale) / alphas[i]

    # Find frequency cutoff
    freqs_cutoff = -np.inf  # In Java use `Integer.MIN_VALUE` or smth like that
    bandwidth = window_bandwidth(window)

    for i in range(num_freqs):
        possible_cutoff = freqs[i] * (1 + 0.5 * bandwidth / Q[i]) + 0.5 * gammas[i]
        freqs_cutoff = max(possible_cutoff, freqs_cutoff)

    # Convert frequencies to filter lengths
    lengths = [0] * num_freqs  # In Java do smth like `double[] lengths = new double[num_freqs]`

    for i in range(num_freqs):
        lengths[i] = Q[i] * sr / (freqs[i] + gammas[i] / alphas[i])

    # Return frequency cutoff and wavelet lengths
    return lengths, freqs_cutoff  # Todo: oh no how to return multiple values in Java???


def compute_wavelet_basis(freqs, sr=22050, window="hann", filter_scale=1, pad_fft=True, norm=1, dtype=np.complex64, gamma=0, alpha=None):
    """
    Construct a wavelet basis using windowed complex sinusoids.

    This function constructs a wavelet filterbank at a specified set of center
    frequencies.

    Taken from https://librosa.org/doc/0.9.1/_modules/librosa/filters.html#wavelet
    """

    # Pass-through parameters to get the filter lengths
    lengths, _ = compute_wavelet_lengths(
        freqs=freqs,
        sr=sr,
        window=window,
        filter_scale=filter_scale,
        gamma_value=gamma,
        fallback_alpha=alpha,
    )

    # Build the filters
    filters = []
    for ilen, freq in zip(lengths, freqs):
        # Build the filter
        # (Note: length will be `ceil(ilen)`)
        temp = np.arange(-ilen // 2, ilen // 2, dtype=float) * 1j * 2 * np.pi * freq / sr  # Note: `j` is the imaginary unit
 
        sig = np.exp(  # Can use `Complex.exp` in Java
            temp
        )

        # Apply the windowing function
        sig *= wrappped_window(len(sig), window)

        # Normalize
        sig = normalize(sig, norm)

        # Append current signal to the filter bases
        filters.append(sig)

    # Pad and stack
    max_len = max(lengths)
    if pad_fft:
        max_len = int(2.0 ** (np.ceil(np.log2(max_len))))
    else:
        max_len = int(np.ceil(max_len))

    filters = np.asarray(
        [pad_center(filt, max_len) for filt in filters], dtype=dtype
    )

    return filters, lengths
