# Project Definition

This project implements four different string searching algorithms and compares their performance using benchmarks.
The four algorithms chosen for this project are:

 - Rabin-Karp
 - Knuth-Morris-Pratt
 - Boyer-Moore
 - FM-index

## Algorithm Design and Time Complexity

According to Wikipedia, the aforementioned algorithms should perform
within the following time bounds, where m is the length of the searched
substring and n the length of the whole searchable text:

| Algorithm          | Preprocessing time | Matching time                   |
|--------------------|--------------------|---------------------------------|
| Rabin-Karp         | Θ(m)               | average Θ(n+m), worst Θ((n-m)m) |
| Knuth-morris-Pratt | Θ(m)               | Θ(n)                            |
| Boyer-Moore        | Θ(m+k)             | best Ω(n/m), worst O(mn)        |
| FM-index           | O(n)               | O(m)                            |

## Data Structures and Space Complexity

All of these algorithms employ some kind of an index or a lookup-table.
Most notable of the data structures is probably the FM-index, which has seen
wide-spread usage in bioinformatics. According to Wikipedia, these algorithms
should have the following space-complexities, where k is the size of the alphabet:

| Algorithm          | Space Complexity |
|--------------------|------------------|
| Rabin-Karp         | O(1)             |
| Knuth-morris-Pratt | Θ(m)             |
| Boyer-Moore        | Θ(k)             |
| FM-index           | O(n)             |

## Input and Output

### Algorithms

All of these algorithms accept two pieces of input: the substring to be searched and the searchable text.
Additionally some of the algorithms may need to know the size of the input alphabet. For this reason,
in this project the algorithms will be implemented to work on bytes instead of characters.

All of the algorithms produce a list of locations of the substring in the searchable text as their output. 

### Command-line utilities

This project implements five different command-line executables: a grep-like utility for each of the
algorithms and an utility for running benchmarks for all of the algorithms.
