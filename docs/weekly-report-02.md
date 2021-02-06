# Weekly Report 02

## What has been worked on?

I implemented the Knuth-Morris-Pratt algorithm, added it to the CLI utility
and created a benchmarking system. I also worked on the JavaDoc documentation
quite a bit.

## How has the program progressed?

The program now has all the basic functionality it needs: the grep-like
CLI utility and a benchmarking system. Now I can focus on implementing the rest of the algorithms.

## What did I learn this week?

I guess there is something to take away from the Knut-Morris_-Pratt algorithm, it works kind of backwards to what I was thinking about.
Honesty, I don't think there was all that much to be learned this week.

## What has caused problems?

Not realizing that the `hashCode` method can return negative integers.
This caused a bit of confusion while debugging something that used the HashMap-implementation.

## What will I be working on next?

Mainly on implementing the remaining two search algorithms and improving the benchmarking system.
A good set of benchmarks that brign out the good and the bad things in the algorithms needs to be
designed.
