# Weekly Report 04

## What has been worked on?

 * Generation of a HTML benchmark results page was implemented.
 * The Boyer-Moore search algorithm was implemented.
 * A fast bit shift based variant of the Rabin-Karp algorithm was implemented.
 * Measuring the variance of benchmark iterations was implemented.
 * Improvements in the `StringMatcher` class and implementation of a `RingBuffer`.

## How has the program progressed?

The command-line utility has seen too little attention, but otherwise the progress has been good.

## What did I learn this week?

That the result of a modulo operation can be a negative integer in Java.
I switched to usin IntelliJ IDEA, so I learned to use it's debugging features.

## What has caused problems?

A lot of edge cases and n-off bugs when reimplementing the buffering logic using a single ring buffer implementation.
Not really any major problems, but a lot of small debugging.

## What will I be working on next?

Reworking the command-line utility and implementing the FM-index algorithm.
I'll probably end up implementing more than the four search algorithms defined
in the project definition, so I'll probably look into those. 

More statistics are always fun, so I'll probably implement measuring the bytes per second
speed for the algorithms. Also I need to look into how the algorithms work when the input
stream is in different sized chunks.

My implementation of the Boyer-Moore algorithm seems rather slow, so that might need some working.
I mean, there's no way it supposed to be slower than my own naive search algorithm, right?
