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

import org.junit.Before;
import org.junit.Test;
import r.lang.*;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

public class CombineFunctionTest {
  private CombineFunction fn;


  @Test
  public void realList() {
    SEXP exp = fn.combine(ListExp.fromArray(new RealExp(1), new RealExp(2), new RealExp(3)));

    assertThat(exp, instanceOf(RealExp.class));
    assertThat(exp.length(), equalTo(3));
  }

  @Test
  public void realsAndLogicalsMixed() {
    SEXP exp = fn.combine(ListExp.fromArray(new RealExp(1), new RealExp(2), NilExp.INSTANCE, new LogicalExp(false)));

    assertThat(exp, instanceOf(RealExp.class));
    assertThat(exp.length(), equalTo(3));
    assertThat(((RealExp)exp).get(2), equalTo(0d));
  }

  @Before
  public void setUp() throws Exception {
    fn = new CombineFunction();
  }
}
