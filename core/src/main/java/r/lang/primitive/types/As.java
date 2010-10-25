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

package r.lang.primitive.types;

import com.google.common.annotations.VisibleForTesting;
import r.lang.EnvExp;
import r.lang.EvalResult;
import r.lang.LangExp;
import r.lang.SEXP;
import r.lang.primitive.UnaryFunction;

public class As {

  public static class Character extends UnaryFunction {
    @Override
    protected EvalResult apply(LangExp call, EnvExp rho, SEXP arg) {
      return new EvalResult(apply(arg));
    }

    @VisibleForTesting
    SEXP apply(SEXP arg) {
      return new CoerceArgToString(arg).coerce();
    }
  }

}
