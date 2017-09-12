#!/bin/sh
#
# This script is used to count lattice points in and integrate over polytopes
#
LATTE_HOME=.
EHRHART_TARGET=.
if [ ! "$#" -gt 0 ]
  then
    echo "ehrhart <polytope> [polynomial]"
    exit 0
fi
sed '/^#/d;' $1 > $1.post
$LATTE_HOME/bin/count --homog $1.post
if [ "$#" -eq 2 ]
  then
    $LATTE_HOME/bin/integrate --valuation=top-ehrhart --monomials=$2 $1.post
    sed 's/\/usr\/local\/share\/latte-int\///' compute-top-ehrhart.mpl > $EHRHART_TARGET/compute-top-ehrhart.mpl
fi