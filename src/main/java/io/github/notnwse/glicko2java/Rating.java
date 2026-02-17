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

public record Rating(
    double rating,
    double deviation,
    double volatility) {

  public static final double DEFAULT_RATING = 1500.0;
  public static final double DEFAULT_DEVIATION = 350.0;
  public static final double DEFAULT_VOLATILITY = 0.06;

  public Rating {
    if (deviation < 0.0) {
      throw new IllegalArgumentException("deviation must be non-negative");
    }
    if (volatility < 0.0) {
      throw new IllegalArgumentException("volatility must be non-negative");
    }
  }

  public static Rating newDefault() {
    return new Rating(DEFAULT_RATING, DEFAULT_DEVIATION, DEFAULT_VOLATILITY);
  }

  public static Rating of(double rating, double deviation, double volatility) {
    return new Rating(rating, deviation, volatility);
  }
}
