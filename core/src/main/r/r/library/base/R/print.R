#  File src/library/base/R/print.R
#  Part of the R package, http://www.R-project.org
#
#  This program is free software; you can redistribute it and/or modify
#  it under the terms of the GNU General Public License as published by
#  the Free Software Foundation; either version 2 of the License, or
#  (at your option) any later version.
#
#  This program is distributed in the hope that it will be useful,
#  but WITHOUT ANY WARRANTY; without even the implied warranty of
#  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#  GNU General Public License for more details.
#
#  A copy of the GNU General Public License is available at
#  http://www.r-project.org/Licenses/

print <- function(x, ...) UseMethod("print")

##- Need '...' such that it can be called as  NextMethod("print", ...):
print.default <- function(x, digits = NULL, quote = TRUE, na.print = NULL,
                          print.gap = NULL, right = FALSE, max = NULL,
                          useSource = TRUE, ...)
{
    noOpt <- missing(digits) && missing(quote) && missing(na.print) &&
	missing(print.gap) && missing(right) && missing(max) &&
	missing(useSource) && length(list(...)) == 0L
    .Internal(print.default(x, digits, quote, na.print, print.gap, right, max,
			    useSource, noOpt))
}

prmatrix <-
    function (x, rowlab = dn[[1]], collab = dn[[2]],
              quote = TRUE, right = FALSE,
              na.print = NULL, ...)
{
    x <- as.matrix(x)
    dn <- dimnames(x)
    .Internal(prmatrix(x, rowlab, collab, quote, right, na.print))
}

noquote <- function(obj) {
    ## constructor for a useful "minor" class
    if(!inherits(obj,"noquote")) class(obj) <- c(attr(obj, "class"),"noquote")
    obj
}

as.matrix.noquote <- function(x, ...) noquote(NextMethod("as.matrix", x))
c.noquote <- function(..., recursive = FALSE)
    structure(NextMethod("c"), class = "noquote")

"[.noquote" <- function (x, ...) {
    attr <- attributes(x)
    r <- unclass(x)[...] ## shouldn't this be NextMethod?
    attributes(r) <- c(attributes(r),
		       attr[is.na(match(names(attr),
                                        c("dim","dimnames","names")))])
    r
}

print.noquote <- function(x, ...) {
    if(!is.null(cl <- attr(x, "class"))) {
	cl <- cl[cl != "noquote"]
        attr(x, "class") <-
          (if(length(cl)) cl else NULL)
      }
    print(x, quote = FALSE, ...)
}

## for alias:
print.listof <- function(x, ...)
{
    nn <- names(x)
    ll <- length(x)
    if(length(nn) != ll) nn <- paste("Component", seq.int(ll))
    for(i in seq_len(ll)) {
	cat(nn[i], ":\n"); print(x[[i]], ...); cat("\n")
    }
    invisible(x)
}

`[.listof` <- `[.AsIs`

## used for version:
print.simple.list <- function(x, ...)
    print(noquote(cbind("_"=unlist(x))), ...)

`[.simple.list` <- `[.listof`

print.function <- function(x, useSource = TRUE, ...)
    .Internal(print.function(x, useSource, ...))
