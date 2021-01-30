# Weekly Report 01

## What has been worked on?

Most of the time was spent on implementing basic supporting
data structures, such as `HashMap` and `ArrayList`, and implementing
the Rabin-Karp algorithm.  There has been also initial work
on the command-line utility.

## How has the program progressed?

Progress has been good and no major road-blocks or issues were encountered.
The main objective of this project is to compare performance of the selected
algorithms. However, I have not yet began working on the benchmarking tooling.

## What did I learn this week?

I think the most interesting thing has been the simple rolling hashing function
used by the current Rabin-Karp implementation. It may prove itself useful in
situations where a quick-and-dirty hash function is needed.

## What has caused problems?

There hasn't really been any considerable problems during this week.
Maybe the most annoying problem has been trying to deal with Java's weak
type system and type erasure in the context of arrays. Although I understand
the problems and why things are as they are, generics in Java are still annoying.

## What will I be working on next?

Tasks for the next week will be getting started on the benchmarking tooling,
polishing the command-line utility and implementing a new algorithm.
