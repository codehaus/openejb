DATE=$(date +%Y%m%d)
VERSION="1.0-$DATE"
RELEASE_ID="openejb-$VERSION"
DIST=$PWD/dist
BIN_DIR=target

function shash { openssl $1 < $2 > $2.$1 ;}
function fail () { echo $1 >&2; exit 1;}
function package () {
    DEST=$1; SOURCE=$2
    tar czf $DEST.tar.gz $SOURCE
    zip -9rq $DEST.zip $SOURCE
}
function replace_version { # VERSION, PROJ
    local VERSION=${1?Must specify a new version name}
    perl -i.bak -pe "s/<(currentVersion)>[^<]*<\/currentVersion>.*/<\$1>${VERSION}<\/\$1>/" etc/project.xml
    perl -i.bak -pe "s/<(version)>1.0-SNAPSHOT<\/version>.*/<\$1>${VERSION}<\/\$1>/" examples/moviefun/project.xml
}

[ -d "$DIST" ] && rm -r $DIST
mkdir -p $DIST

cvs -d ':pserver:anoncvs@cvs.openejb.org:/home/projects/openejb/scm' co -d $RELEASE_ID openejb1


( cd $RELEASE_ID && replace_version $VERSION )

package $DIST/${RELEASE_ID}-src $RELEASE_ID || fail "Unable to create source binaries"

( cd $RELEASE_ID && maven -o -Dmaven.{itest,test}.skip=true ) || fail "Build failed"

( cd $RELEASE_ID/$BIN_DIR && package $DIST/${RELEASE_ID} $RELEASE_ID ) || fail "Unable to make binary archives"

for n in $DIST/*; do
    shash md5 $n
done

scp -r $DIST openejb.org:/home/projects/openejb/public_html/unstable/v$VERSION

( cd $RELEASE_ID

OPTS='-Dmaven.remote.group=openejb -Dmaven.repo.central=beaver.codehaus.org -Dmaven.repo.central.directory=/dist'

for n in modules/{core,loader}; do
    (cd $n && maven -o -Dmaven.username=${USER} $OPTS jar:deploy)
done
)

