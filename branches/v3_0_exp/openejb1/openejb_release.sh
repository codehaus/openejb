RELEASE_ID='openejb-1.0-beta1'
DIST=$PWD/dist
BIN_DIR=target

mkdir -p $DIST

cvs -d ':pserver:anoncvs@cvs.openejb.org:/home/projects/openejb/scm' co -r v1_0beta1 -d $RELEASE_ID openejb1

function shash { openssl $1 < $2 > $2.$1 ;}
function fail () { echo $1 >&2; exit 1;}
function package () {
    DEST=$1; SOURCE=$2
    tar czf $DEST.tar.gz $SOURCE
    zip -9rq $DEST.zip $SOURCE
}

package $DIST/${RELEASE_ID}-src $RELEASE_ID || fail "Unable to create source binaries"

( cd $RELEASE_ID && maven -o -Dmaven.{itest,test}.skip=true ) || fail "Build failed"

( cd $RELEASE_ID/$BIN_DIR && package $DIST/${RELEASE_ID} $RELEASE_ID ) || fail "Unable to make binary archives"
