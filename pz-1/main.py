from typing import Any


def min_of_three[T: (int, float, str)](a: T, b: T, c: T) -> T:
    v1 = a if a < b else b
    v2 = v1 if v1 < c else c
    return v2


def reverce_str(s: str) -> str:
    return s[::-1]


def is_palindrome(s: str) -> bool:
    for i in range(len(s) // 2):
        if s[i] != s[-(i + 1)]:
            return False
    return True


def quicksort[T: Any](arr: list[T]) -> list[T]:
    if len(arr) <= 1:
        return arr

    pivot = arr[len(arr) // 2]

    left = [x for x in arr if x < pivot]
    middle = [x for x in arr if x == pivot]
    right = [x for x in arr if x > pivot]

    return quicksort(left) + middle + quicksort(right)


if __name__ == "__main__":
    assert min_of_three(7, 5, 10) == 5

    assert reverce_str("hello") == "olleh"

    assert not is_palindrome("hello")
    assert is_palindrome("olhlo")
    assert is_palindrome("ollo")

    assert quicksort([]) == []
    assert quicksort([1]) == [1]
    assert quicksort([2, 1]) == [1, 2]
    assert quicksort([3, 6, 8, 10, 1, 2]) == [1, 2, 3, 6, 8, 10]
