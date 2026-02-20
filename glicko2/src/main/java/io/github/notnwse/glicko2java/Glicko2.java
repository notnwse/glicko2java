/*
 * Copyright (c) 2026 notnwse
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.github.notnwse.glicko2java;

import java.util.List;

public final class Glicko2 {

  private static final double SCALE = 173.7178;
  private static final double EPSILON = 0.000001;
  private static final double PI_SQ = Math.PI * Math.PI;
  private static final double DEFAULT_TAU = 0.5;

  private final double tau;
  private final double tauSq;

  private Glicko2(double tau) {
    this.tau = tau;
    this.tauSq = tau * tau;
  }

  public static Glicko2 create() {
    return create(DEFAULT_TAU);
  }

  public static Glicko2 create(double tau) {
    if (tau <= 0 || Double.isNaN(tau) || Double.isInfinite(tau)) {
      throw new IllegalArgumentException("tau must be positive and finite");
    }
    return new Glicko2(tau);
  }

  public Rating recalculate(Rating player, List<Match> matches) {
    final double mu = (player.rating() - 1500.0) / SCALE;
    final double phi = player.deviation() / SCALE;
    final double sigma = player.volatility();
    final double phiSq = phi * phi;

    if (matches == null || matches.isEmpty()) {
      final double newPhi = Math.sqrt(phiSq + (sigma * sigma));
      return this.convertToOriginalScale(mu, newPhi, sigma);
    }

    double vInv = 0.0;
    double deltaSum = 0.0;

    // noinspection ForLoopReplaceableByForEach
    for (int idx = 0; idx < matches.size(); idx++) {
      final Match match = matches.get(idx);
      final double muJ = (match.opponent().rating() - 1500.0) / SCALE;
      final double phiJ = match.opponent().deviation() / SCALE;

      final double gPhiJ = 1.0 / Math.sqrt(1.0 + (3.0 * phiJ * phiJ) / PI_SQ);
      final double eTerm = 1.0 / (1.0 + Math.exp(-gPhiJ * (mu - muJ)));

      vInv += (gPhiJ * gPhiJ) * eTerm * (1.0 - eTerm);
      deltaSum += gPhiJ * (match.score() - eTerm);
    }

    final double v = 1.0 / vInv;
    final double delta = v * deltaSum;

    final double newSigma = this.determineNewVolatility(sigma, phiSq, delta, v);

    final double phiStarSq = phiSq + (newSigma * newSigma);
    final double newPhi = 1.0 / Math.sqrt((1.0 / phiStarSq) + vInv);
    final double newMu = mu + (newPhi * newPhi) * deltaSum;

    return this.convertToOriginalScale(newMu, newPhi, newSigma);
  }

  private double determineNewVolatility(double sigma, double phiSq, double delta, double v) {
    final double a = Math.log(sigma * sigma);
    final double deltaSq = delta * delta;

    double A = a;
    double B;

    if (deltaSq > phiSq + v) {
      B = Math.log(deltaSq - phiSq - v);
    } else {
      int k = 1;
      while (true) {
        final double x = a - k * this.tau;

        final double ex = Math.exp(x);
        final double d = phiSq + v + ex;
        final double fx = (ex * (deltaSq - phiSq - v - ex)) / (2.0 * d * d) - (x - a) / this.tauSq;

        if (fx >= 0) {
          B = x;
          break;
        }
        k++;
      }
    }

    double fA = this.f(A, deltaSq, phiSq, v, a);
    double fB = this.f(B, deltaSq, phiSq, v, a);

    while (Math.abs(B - A) > EPSILON) {
      final double C = A + (A - B) * fA / (fB - fA);
      final double fC = this.f(C, deltaSq, phiSq, v, a);

      if (fC * fB <= 0) {
        A = B;
        fA = fB;
      } else {
        fA = fA / 2.0;
      }

      B = C;
      fB = fC;
    }
    return Math.exp(A / 2.0);
  }

  private double f(double x, double deltaSq, double phiSq, double v, double a) {
    final double ex = Math.exp(x);
    final double denomBase = phiSq + v + ex;
    return (ex * (deltaSq - phiSq - v - ex)) / (2.0 * denomBase * denomBase) - (x - a) / this.tauSq;
  }

  private Rating convertToOriginalScale(double mu, double phi, double sigma) {
    return new Rating(
        (mu * SCALE) + 1500.0,
        phi * SCALE,
        sigma
    );
  }
}
