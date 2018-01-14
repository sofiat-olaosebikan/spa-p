===== The Java codes in this directory were implemented by Duncan Milne (Duncan.Milne1@gmail.com) =====
|
The directory contains an implementation of the 3/2-approximation algorithm and 2-approximation algorithm, for the problem of finding a maximum cardinality stable matching,
given an instance of the Student-Project Allocation problem with lecturer preferences over Projects (SPA-P).

Approx.java ------------------ an implementation of the 2-approximation algorithm
ApproxPromotion.java --------- an implementation of the 3/2-approximation algorithm
Digraph.java ----------------- an implementation to verfiy that the matching returned admits no coalition
GurobiModel.java ------------- an implementation of an IP formulation to solve the maximum cardinality stable matching problem optimally
StabilityChecker.java -------- an implementation to verfiy that the matching returned is stable

For an understanding of how these approximation algorithms work, we refer interested readers to the following papers:

1) D.F. Manlove and G. O’Malley. Student project allocation with preferences over projects. Journal
of Discrete Algorithms, 6:553–560, 2008.

2) K. Iwama, S. Miyazaki, and H. Yanagisawa. Improved approximation bounds for the student-
project allocation problem with preferences over projects. In Proceedings of TAMC ’11: the
8th Annual Conference on Theory and Applications of Models of Computation, volume 6648 of
Lecture Notes in Computer Science, pages 440–451. Springer, 2011.
