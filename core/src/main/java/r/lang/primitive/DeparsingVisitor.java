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

package r.lang.primitive;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import r.lang.*;
import r.parser.ParseUtil;

import static com.google.common.collect.Iterables.transform;

public class DeparsingVisitor extends SexpVisitor<String> {

  private StringBuilder deparsed = new StringBuilder();

  public DeparsingVisitor(SEXP exp) {
    exp.accept(this);
  }

  @Override
  public void visit(CharExp charExp) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void visit(ComplexExp complexExp) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void visit(EnvExp envExp) {
    // this is somewhat random; it's isn't parsable in any case
    deparsed.append("<environment>");
  }

  @Override
  public void visit(ExpExp expSexp) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void visit(BuiltinExp builtinSexp) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void visit(IntExp intExp) {
    appendVector(intExp, new ParseUtil.IntDeparser());
  }

  @Override
  public void visit(ListExp listExp) {
    deparsed.append("list(");
    boolean needsComma = false;
    for(SEXP sexp : listExp) {
      if(needsComma) {
        deparsed.append(", ");
      } else {
        needsComma = true;
      }
      sexp.accept(this);
    }
    deparsed.append(")");
  }

  @Override
  public void visit(NilExp nilExp) {
    deparsed.append("NULL");
  }

  @Override
  public void visit(PrimitiveSexp primitiveSexp) {
    super.visit(primitiveSexp);
  }

  @Override
  public void visit(PromExp promExp) {
    super.visit(promExp);
  }

  @Override
  public void visit(RealExp realExp) {
    appendVector(realExp, new ParseUtil.RealDeparser());
  }

  @Override
  public void visit(StringExp stringExp) {
    appendVector(stringExp, new ParseUtil.StringDeparser());
  }

  @Override
  public void visit(LogicalExp logicalExp) {
   appendVector(logicalExp, new ParseUtil.LogicalDeparser());
  }

  @Override
  public void visit(LangExp langExp) {
    super.visit(langExp);
  }

  @Override
  public void visit(SymbolExp symbolExp) {
    deparsed.append(symbolExp.getPrintName());
  }

  @Override
  public void visit(ClosureExp closureExp) {
    throw new UnsupportedOperationException("deparsing of closures not yet implemented");
  }

  @Override
  public String getResult() {
    return deparsed.toString();
  }

  public <T> void appendVector(Iterable<T> values, Function<T, String> deparser) {
    if(Iterables.size(values) == 1 ) {
      deparsed.append(deparser.apply(values.iterator().next()));
    } else {
      Joiner.on(", ").appendTo(deparsed, transform(values, deparser));
    }
  }

}
