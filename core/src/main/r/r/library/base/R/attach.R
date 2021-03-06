#  File src/library/base/R/attach.R
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

attach <- function(what, pos = 2, name = deparse(substitute(what)),
                   warn.conflicts = TRUE)
{
    checkConflicts <- function(env)
    {
        dont.mind <- c("last.dump", "last.warning", ".Last.value",
                       ".Random.seed", ".First.lib", ".Last.lib",
                       ".packageName", ".noGenerics", ".required",
                       ".no_S3_generics")
        sp <- search()
        for (i in seq_along(sp)) {
            if (identical(env, as.environment(i))) {
                db.pos <- i
                break
            }
        }
        ob <- objects(db.pos, all.names = TRUE)
        if(.isMethodsDispatchOn()) {
            ## <FIXME>: this is wrong-headed: see library().
            these <- objects(db.pos, all.names = TRUE)
            these <- these[substr(these, 1L, 6L) == ".__M__"]
            gen <- gsub(".__M__(.*):([^:]+)", "\\1", these)
            from <- gsub(".__M__(.*):([^:]+)", "\\2", these)
            gen <- gen[from != ".GlobalEnv"]
            ob <- ob[!(ob %in% gen)]
        }
        ipos <- seq_along(sp)[-c(db.pos, match(c("Autoloads", "CheckExEnv"), sp, 0L))]
        for (i in ipos) {
            obj.same <- match(objects(i, all.names = TRUE), ob, nomatch = 0L)
            if (any(obj.same > 0)) {
                same <- ob[obj.same]
                same <- same[!(same %in% dont.mind)]
                Classobjs <- grep("^\\.__", same)
                if(length(Classobjs)) same <- same[-Classobjs]
                ## report only objects which are both functions or
                ## both non-functions.
                is_fn1 <- sapply(same, function(x)
                                 exists(x, where = i, mode = "function",
                                        inherits = FALSE))
                is_fn2 <- sapply(same, function(x)
                                 exists(x, where = db.pos, mode = "function",
                                        inherits = FALSE))
                same <- same[is_fn1 == is_fn2]
                if(length(same)) {
                    cat("\n\tThe following object(s) are masked",
                        if (i < db.pos) "_by_" else "from", sp[i],
                        if (sum(sp == sp[i]) > 1L) paste("( position", i, ")"),
                        ":\n\n\t", same, "\n\n")
                }
            }
        }
    }

    if(pos == 1) {
        warning("*** 'pos=1' is not possible; setting 'pos=2' for now.\n",
                "*** Note that 'pos=1' will give an error in the future")
        pos <- 2
    }
    if (is.character(what) && (length(what) == 1L)){
        if (!file.exists(what))
            stop(gettextf("file '%s' not found", what), domain = NA)
        name <- paste("file:", what, sep="")
        value <- .Internal(attach(NULL, pos, name))
        load(what, envir=as.environment(pos))
    }
    else
        value <- .Internal(attach(what, pos, name))
    if(warn.conflicts &&
       !exists(".conflicts.OK", envir = value, inherits = FALSE)) {
        checkConflicts(value)
    }
    if( length(objects(envir = value, all.names = TRUE) )
       && .isMethodsDispatchOn())
      methods:::cacheMetaData(value, TRUE)
    invisible(value)
}

detach <- function(name, pos=2, unload=FALSE, character.only = FALSE)
{
    if(!missing(name)) {
	if(!character.only)
	    name <- substitute(name)# when a name..
	pos <-
	    if(is.numeric(name))
                name
	    else {
                if (!is.character(name))
                    name <- deparse(name)
                match(name, search())
            }
	if(is.na(pos))
	    stop("invalid name")
    }
    env <- as.environment(pos)
    packageName <- search()[[pos]]

    ## we need treat packages differently from other objects.
    isPkg <- grepl("^package:", packageName)
    if (!isPkg) {
        .Internal(detach(pos))
        return(invisible())
    }

    pkgname <- sub("^package:", "", packageName)
    libpath <- attr(env, "path")
    hook <- getHook(packageEvent(pkgname, "detach")) # might be list()
    for(fun in rev(hook)) try(fun(pkgname, libpath))
    if(exists(".Last.lib", mode = "function", where = pos, inherits=FALSE)) {
        .Last.lib <- get(".Last.lib",  mode = "function", pos = pos,
                         inherits=FALSE)
        if(!is.null(libpath)) try(.Last.lib(libpath))
    }
    .Internal(detach(pos))

    ## Check for detaching a package require()d by another package (not
    ## by .GlobalEnv because detach() can't currently fix up the
    ## .required there)
    for(pkg in search()[-1L]) {
        ## How can a namespace environment get on the search list?
        ## (base fails isNamespace).
        if(!isNamespace(as.environment(pkg)) &&
           exists(".required", pkg, inherits = FALSE) &&
           pkgname %in% get(".required", pkg, inherits = FALSE))
            warning(packageName, " is required by ", pkg, " (still attached)")
    }

    if(pkgname %in% loadedNamespaces()) {
        ## the lazyload DB is flushed when the name space is unloaded
        if(unload) {
            tryCatch(unloadNamespace(pkgname),
                     error = function(e)
                     warning(pkgname, " namespace cannot be unloaded\n",
                             conditionMessage(e), call. = FALSE))
        }
    } else
        .Call("R_lazyLoadDBflush",
              paste(libpath, "/R/", pkgname, ".rdb", sep=""),
              PACKAGE="base")
    invisible()
}

ls <- objects <-
    function (name, pos = -1, envir = as.environment(pos), all.names = FALSE,
              pattern)
{
    if (!missing(name)) {
        nameValue <- try(name)
        if(identical(class(nameValue), "try-error")) {
            name <- substitute(name)
            if (!is.character(name))
                name <- deparse(name)
            pos <- name
        }
        else
            pos <- nameValue
    }
    all.names <- .Internal(ls(envir, all.names))
    if (!missing(pattern)) {
        if ((ll <- length(grep("[", pattern, fixed=TRUE))) &&
            ll != length(grep("]", pattern, fixed=TRUE))) {
            if (pattern == "[") {
                pattern <- "\\["
                warning("replaced regular expression pattern '[' by  '\\\\['")
            }
            else if (length(grep("[^\\\\]\\[<-", pattern))) {
                pattern <- sub("\\[<-", "\\\\\\[<-", pattern)
                warning("replaced '[<-' by '\\\\[<-' in regular expression pattern")
            }
        }
        grep(pattern, all.names, value = TRUE)
    }
    else all.names
}
