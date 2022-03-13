# IMPORTS
import numpy as np
import numba

from vqt_from_scratch.helpers import get_filter
from vqt_from_scratch.utils import fix_length


# FUNCTIONS
def resample(x, sr_orig, sr_new, res_type="kaiser_best", scale=False):  # To be implemented in `Audio` class; Todo: implement
    """
    Resample a signal x from orig_sr to target_sr.

    Taken from https://github.com/bmcfee/resampy/blob/ccb85575403663e17697bcde8e25765b022a9e0f/resampy/core.py
    """

    # Validate inputs
    if sr_orig <= 0:
        raise ValueError(f"Invalid sample rate: sr_orig={sr_orig}")

    if sr_new <= 0:
        raise ValueError(f"Invalid sample rate: sr_new={sr_new}")

    # Calculate sample ratio
    sample_ratio = float(sr_new) / sr_orig

    # Set up the output shape
    shape = list(x.shape)
    
    if int(shape[-1] * sample_ratio) < 1:
        raise ValueError(f"Input signal length={x.shape[-1]} is too small to resample from {sr_orig} to {sr_new}")
    else:
        shape[-1] = int(shape[-1] * sample_ratio)

    # Preserve contiguity of input (if it exists). If not, revert to C-contiguity by default
    # Todo: does this matter in Java?
    if x.flags["F_CONTIGUOUS"]:
        order = "F"
    else:
        order = "C"

    # Generate output array in storage
    y = np.zeros(shape, dtype=x.dtype, order=order)  # Todo: does `order` matter in Java?

    # Get the interpolation window and precision of the specified `res_type`
    interp_win, precision, _ = get_filter(res_type)

    # Treat the interpolation window
    if sample_ratio < 1:
        interp_win *= sample_ratio  # Multiply every element in the window by `sample_ratio`

    # Calculate interpolation deltas
    interp_delta = np.zeros_like(interp_win)  # Makes `interp_delta` have the same shape and type as `interp_win`
    interp_delta[:-1] = np.diff(interp_win)  # Calculates `interp_win[i+1] - interp_win[i]` for all `0 <= i < interp_win.length - 1`

    # Construct 2d views of the data with the resampling axis on the first dimension
    # (Todo: find a way to implement this in Java)
    x_2d = x.swapaxes(0, -1).reshape((x.shape[-1], -1))  # NOTE: Any changes made here WILL BE REFLECTED IN `x`!
    y_2d = y.swapaxes(0, -1).reshape((y.shape[-1], -1))  # NOTE: Any changes made here WILL BE REFLECTED IN `y`!


    # Run resampling
    resample_f(x_2d, y_2d, sample_ratio, interp_win, interp_delta, precision)

    # Fix the length of the samples array
    n_samples = int(np.ceil(x.shape[-1] * sample_ratio))
    y_hat = fix_length(y, n_samples)

    # Handle rescaling
    if scale:
        y_hat /= np.sqrt(sample_ratio)  # Basically divide everything in `y` by that

    # Return the resampled array
    return y_hat


@numba.jit(nopython=True, nogil=True)  # TODO: This makes this function SO MUCH FASTER. How to make it work in Java?
def resample_f(x, y, sample_ratio, interp_win, interp_delta, precision):  # Helper function
    """Taken from https://github.com/bmcfee/resampy/blob/ccb85575403663e17697bcde8e25765b022a9e0f/resampy/interpn.py"""

    # Define constants that will be needed later
    scale = min(1.0, sample_ratio)
    time_increment = 1./sample_ratio
    index_step = int(scale * precision)

    nwin = interp_win.shape[0]
    n_orig = x.shape[0]
    n_out = y.shape[0]
    n_channels = y.shape[1]

    # Define 'loop variables'
    n = 0
    frac = 0.0
    index_frac = 0.0
    offset = 0
    eta = 0.0
    weight = 0.0
    time_register = 0.0

    for t in range(n_out):
        # Grab the top bits as an index to the input buffer
        n = int(time_register)

        # Grab the fractional component of the time index
        frac = scale * (time_register - n)

        # Offset into the filter
        index_frac = frac * precision
        offset = int(index_frac)

        # Interpolation factor
        eta = index_frac - offset

        # Compute the left wing of the filter response
        i_max = min(n + 1, (nwin - offset) // index_step)
        for i in range(i_max):
            weight = (interp_win[offset + i * index_step] + eta * interp_delta[offset + i * index_step])
            for j in range(n_channels):
                y[t, j] += weight * x[n - i, j]

        # Invert P
        frac = scale - frac

        # Offset into the filter
        index_frac = frac * precision
        offset = int(index_frac)

        # Interpolation factor
        eta = index_frac - offset

        # Compute the right wing of the filter response
        k_max = min(n_orig - n - 1, (nwin - offset)//index_step)
        for k in range(k_max):
            weight = (interp_win[offset + k * index_step] + eta * interp_delta[offset + k * index_step])
            for j in range(n_channels):
                y[t, j] += weight * x[n + k + 1, j]

        # Increment the time register
        time_register += time_increment
