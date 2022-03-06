# IMPORTS
import numpy as np


# FUNCTIONS
def normalize(s, norm):
    """
    Normalizes `s`.
    
    Assumes `s` is a 1d array.
    Todo: verify that `s` is a 1d array
    """

    # Set threshold to be the smallest non-zero number supported
    threshold = tiny(s)

    # Get the magnitudes of the data in `s`
    mag = np.array([abs(x) for x in s])

    # Compute `length` and `fill_norm`
    if norm == 0:
        # length = np.sum(mag > 0, axis=0, keepdims=True) ** (1.0 / norm)  # Todo: convert this into java-compatible form
        length = np.sum(mag, axis=0, keepdims=True) ** (1.0 / norm)  # Is this the same as above? Todo: convert this into java-compatible form
        fill_norm = 1
    elif norm < 0:
        raise ParameterError(f"Unsupported norm: {norm}")
    else:
        length = np.sum(mag ** norm, axis=0, keepdims=True) ** (1.0 / norm)  # Todo: convert this into java-compatible form
        fill_norm = mag.shape[0] ** (-1.0 / norm)  # Axis is zero

    # Get the indices where the index is below the threshold
    small_idx = []
    for i in range(len(length)):
        if length[i] < threshold:
            small_idx.append(i)

    # Normalise the signal, but leave small indices un-normalized
    s_norm = np.empty_like(s)   # What is the Java-equivilant of this?

    for index in small_idx:
        length[index] = 1.0

    for i in range(len(s)):
        s_norm[i] = s[i] / length

    # Return the normalised signal
    return s_norm


def tiny(x):
    """
    Compute the tiny-value corresponding to an input's data type.
    
    Taken from https://librosa.org/doc/0.9.1/_modules/librosa/util/utils.html#tiny
    """

    # Make sure we have an array view
    x = np.asarray(x)

    # Only floating types generate a tiny
    if np.issubdtype(x.dtype, np.floating) or np.issubdtype(x.dtype, np.complexfloating):
        dtype = x.dtype
    else:
        dtype = np.float32

    return np.finfo(dtype).tiny


def fix_length(data, size):
    """
    Fix the length an array `data` to exactly `size` along a target axis.

    If `data.shape[axis] < n`, pad according to the provided kwargs.
    By default, `data` is padded with trailing zeros.

    Assumes `data` is a one dimensional array.
    """

    # Get length of data
    n = len(data)

    # Handle separately the different cases
    output_data = [0] * size

    if n > size:
        # Keep only until `size` in the data
        for i in range(size):
            output_data[i] = data[i]

    elif n < size:
        # Fill in the first `n` entries
        for i in range(n):
            output_data[i] = data[i]

    # Return the output data
    return np.array(output_data)  # May not be needed in Java


def pad_center(data, size):
    """
    Pad an array to a target length along a target axis.

    This differs from `np.pad` by centering the data prior to padding,
    analogous to `str.center`
    """

    # Get length of data
    n = len(data)

    # Assert that the length of the data at least the desired size
    if n > size:
        raise ParameterError(f"Target size ({size}) must be at least input size ({n})")

    # Calculate left padding
    # (Right padding is everything left over)
    lpad = int((size - n) // 2)

    # Fill in the output data
    output_data = [0] * size

    for i in range(n):
        output_data[lpad + i] = data[i]  # Consider the left padding

    # Return data
    return np.array(output_data)  # May not be needed in Java

