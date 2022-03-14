# IMPORTS
import numpy as np

from filter_data import kaiserBest, kaiserFast  # This is loaded from JSON files


# FUNCTIONS 
def get_filter(filter_):
    """
    Gets the data from the required filter.
    """

    filter_data = None
    if filter_ == "kaiser_best":
        filter_data = kaiserBest
    elif filter_ == "kaiser_fast":
        filter_data = kaiserFast

    return np.array(filter_data["halfwin"], dtype=np.float32), filter_data["perc"], filter_data["rolloff"]


def num_two_factors(x):  # Now in `MathUtils`
    """
    Return how many times integer x can be evenly divided by 2.
    Returns 0 for non-positive integers.
    """

    if x <= 0:
        return 0
    num_twos = 0 
    while x % 2 == 0:
        num_twos += 1
        x //= 2

    return num_twos


def compute_alpha(bins_per_octave):
    """
    Computes the alpha coefficient as described in:
        Glasberg, Brian R., and Brian CJ Moore. "Derivation of auditory filter shapes from notched-noise data."
        Hearing research 47.1-2 (1990): 103-138.
    """

    r = 2 ** (1 / bins_per_octave)  # Should be handled as a double in Java
    return (r ** 2 - 1) / (r ** 2 + 1)
