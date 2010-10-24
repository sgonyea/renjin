/*
 * R : A Computer Language for Statistical Data Analysis
 * Copyright (C) 1995, 1996  Robert Gentleman and Ross Ihaka
 * Copyright (C) 1997--2008  The R Development Core Team
 * Copyright (C) 2003, 2004  The R Foundation
 * Copyright (C) 2010 bedatadriven
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package r.lang;

/**
 * The result of an evaluation.
 */
public class EvalResult {

  private final SEXP expression;
  private final boolean visible;

  public static EvalResult NON_PRINTING_NULL = new EvalResult(NilExp.INSTANCE, false);

  public EvalResult(SEXP expression) {
    this.expression = expression;
    this.visible = true;
  }

  public EvalResult(SEXP expression, boolean visible) {
    this.expression = expression;
    this.visible = visible;
  }

  /**
   * @return  the expression that has resulted from
   * evaluation of the function
   */
  public SEXP getExpression() {
    return expression;
  }

  /**
   * @return whether the result should be printed to a console
   */
  public boolean isVisible() {
    return visible;
  }
}
