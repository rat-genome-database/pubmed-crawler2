# download pubmed articles for given date range
# f.e. to download articles from PubMed created/modified during date range Jan 01, 2015 through Jan 30, 2015
# ./run.sh 2015/01/01 2015/01/30
#
. /etc/profile
source ~/.bashrc
MYJAVA="java -Xms512m -Xmx20480m"
APP_DIR=/rgd/pubmed/PubMedCrawler2
DATA_DIR=/rgd/pubmed/

cd $APP_DIR
$MYJAVA -jar lib/PubMedCrawler2.jar --crawlByDate $DATA_DIR $1 $2
