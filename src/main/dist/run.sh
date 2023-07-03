# download pubmed articles for given date range
# f.e. to download articles from PubMed created/modified during date range Jan 01, 2015 through Jan 30, 2015
# ./run.sh 2015/01/01 2015/01/30
#
. /etc/profile
MYJAVA="java -Xms512m -Xmx20480m"
APP_DIR=/home/rgddata/pipelines/PMCCrawlerPipeline
DATA_DIR=$APP_DIR/data/

cd $APP_DIR
$MYJAVA -jar lib/PMCCrawlerPipeline.jar --crawlByDate $DATA_DIR $1 $2
