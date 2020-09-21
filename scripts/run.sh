# download pubmed articles for given date range
# f.e. to download articles from PubMed created/modified during date range Jan 01, 2015 through Jan 30, 2015
# ./run.sh 2015/01/01 2015/01/30
#
MYJAVA="java -Xms512m -Xmx20480m"
APP_DIR=/rgd/pubmed
CRAWLER_DIR=$APP_DIR/PubMedCrawler2

cd $CRAWLER_DIR
$MYJAVA -jar PubmedCrawler2.jar crawlByDate $APP_DIR $1 $2
