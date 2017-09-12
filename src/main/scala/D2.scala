/*
Zlib license - https://opensource.org/licenses/Zlib

Copyright (c) <2017> <David Ruescas>

This software is provided 'as-is', without any express or implied
warranty. In no event will the authors be held liable for any damages
arising from the use of this software.

Permission is granted to anyone to use this software for any purpose,
including commercial applications, and to alter it and redistribute it
freely, subject to the following restrictions:

1. The origin of this software must not be misrepresented; you must not
   claim that you wrote the original software. If you use this software
   in a product, an acknowledgment in the product documentation would be
   appreciated but is not required.
2. Altered source versions must be plainly marked as such, and must not be
   misrepresented as being the original software.
3. This notice may not be removed or altered from any source distribution.
*/

import java.math.BigInteger
import java.math.BigDecimal
import java.math.RoundingMode

/**
 *  Calculates degree of privacy in weighted voting with 2 options (eg Yes,No)
 *
 *  Usage:
 *
 *  <D2> <points-file> <group-sizes>
 *
 *  The points file is generated with polymake, using
 *
 *  polymake --script election1.polymake > election1.points
 *
 *  Example polymake script:
 *
 *  use application 'polytope';
 *  my $e1 = new Polytope(INEQUALITIES=>[[15, -1, 0, 0, 0],[10, 0, -1, 0, 0],[20, 0, 0, -1, 0],[30, 0, 0, 0, -1],[0, 1, 0, 0, 0],[0, 0, 1, 0, 0],[0, 0, 0, 1, 0],[0, 0, 0, 0, 1]],EQUATIONS=>[[-1503, 61, 24, 18, 12]]);
 *  print $e1->LATTICE_POINTS;*
 *
 *  whose equivalent .lp format is
 *
 *  Subject To
 *
 *    61a + 24b + 18c + 12d = 1503
 *  Bounds
 *    0 <= a <= 15
 *    0 <= b <= 10
 *    0 <= c <= 20
 *    0 <= d <= 30
 *
 *  General
 *    a b c d
 *  End
 *
 */
object D2 extends App {

  val lines = scala.io.Source.fromFile(args(0)).getLines
  val voters = args.slice(1, args.length).map(_.toInt)

  println(s"Processing file '${args(0)}' with groups [${voters.mkString(" ")}]..")

  // we skip the first column and take n number of variables
  val lineValues = lines.map(_.split(' ').slice(1,voters.size + 1))

  // calculate total points for each voter casting Yes, plus overall total
  val totals = lineValues.map { line =>

    val lineInts = line.map(_.trim.toInt)
    val withVoters = lineInts.zip(voters)

    // multiply binomial coefficients for all coordinates
    val total = withVoters.map { case (a, b) => binomial(b, a) }.reduce(_.multiply(_))

    // the fraction of points in which the voters cast Yes
    val points = withVoters.map { case(a, b) =>
      total.multiply(BigInteger.valueOf(a)).divide(BigInteger.valueOf(b))
    }

    // the last array holds the total multiplicities for each point
    points :+ total
  }

  val arr = totals.toArray
  println(s"Found ${arr.size} lattice points")

  // sum multiplicities for each point, m(v, c, r)
  val sums = arr.transpose.map { values =>
    values.reduce(_.add(_))
  }
  // overall multiplicities, Ar
  val allSpace = new BigDecimal(sums(sums.length - 1))
  println(s"Total solutions $allSpace")

  // probabilities = m(v,c, r) / Ar
  val ps = sums.dropRight(1).map(new BigDecimal(_).divide(allSpace, 5, RoundingMode.HALF_UP))
  println("p = " + ps.mkString(" "))
  // entropies
  val hs = ps.map { prob =>
    val p = prob.doubleValue
    val q = 1 - p
    -(p * (Math.log(p) / Math.log(2)) + ((1-p) * Math.log(1-p) / Math.log(2)))
  }
  // degree of privacy, with 2 options Hm = 1, so a = H(x)
  println("d = " + hs.mkString(" ").replace("NaN", "0"))

  /**
   *  Binomial coefficient
   */
  def binomial(n: Int, k: Int): BigInteger = {
    var ret = BigInteger.ONE;
    for(i <- 0 to k-1) {
      ret = ret.multiply(BigInteger.valueOf(n-i)).divide(BigInteger.valueOf(i+1))
    }
    ret
  }
}