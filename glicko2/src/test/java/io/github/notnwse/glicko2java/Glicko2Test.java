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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

class Glicko2Test {

  @Test
  void testRecalculation() {
    final Rating player = Rating.of(Rating.DEFAULT_RATING, 200, Rating.DEFAULT_VOLATILITY);
    final List<Match> matches = List.of(
        Match.of(Rating.of(1400, 30, Rating.DEFAULT_VOLATILITY), Match.WIN),
        Match.of(Rating.of(1550, 100, Rating.DEFAULT_VOLATILITY), Match.LOSS),
        Match.of(Rating.of(1700, 300, Rating.DEFAULT_VOLATILITY), Match.LOSS)
    );
    final Glicko2 glicko2 = Glicko2.createDefault();

    final Rating newRating = glicko2.recalculate(player, matches);

    assertThat(newRating.rating()).isCloseTo(1464.06, offset(0.1));
    assertThat(newRating.deviation()).isCloseTo(151.52, offset(0.1));
    assertThat(newRating.volatility()).isCloseTo(0.05999, offset(0.0001));
  }

  @Test
  void testInactivity() {
    final Rating player = Rating.of(Rating.DEFAULT_RATING, 200, Rating.DEFAULT_VOLATILITY);
    final Glicko2 glicko2 = Glicko2.createDefault();

    final Rating newRating = glicko2.recalculate(player, Collections.emptyList());

    assertThat(newRating.rating()).isEqualTo(Rating.DEFAULT_RATING);
    assertThat(newRating.deviation()).isGreaterThan(200);
    assertThat(newRating.volatility()).isEqualTo(Rating.DEFAULT_VOLATILITY);
  }
}
