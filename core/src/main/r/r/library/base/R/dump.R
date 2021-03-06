#  File src/library/base/R/dump.R
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

dump <- function (list, file = "dumpdata.R", append = FALSE,
		  control = "all", envir = parent.frame(),
		  evaluate = TRUE)
{
    if(is.character(file)) {
	## avoid opening a file if there is nothing to dump
	ex <- sapply(list, exists, envir=envir)
	if(!any(ex)) return(invisible(character(0L)))
	if(nzchar(file)) {
	    file <- file(file, ifelse(append, "a", "w"))
	    on.exit(close(file), add = TRUE)
	} else file <- stdout()
    }
    opts <- .deparseOpts(control)
    .Internal(dump(list, file, envir, opts, evaluate))
}

