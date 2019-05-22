package net.bither.model;

import java.io.Serializable;

public class MarketTicket implements Serializable{


    /**
     * data : {"id":42,"name":"Primecoin","symbol":"XPM","website_slug":"primecoin","rank":181,"circulating_supply":24589301,"total_supply":24589301,"max_supply":null,"quotes":{"USD":{"price":0.8454120128,"volume_24h":243176.783181443,"market_cap":20788090,"percent_change_1h":-1.61,"percent_change_24h":-2.08,"percent_change_7d":-19.85},"CNY":{"price":5.8238742737,"volume_24h":1675196.2239803215,"market_cap":143204995,"percent_change_1h":-1.61,"percent_change_24h":-2.08,"percent_change_7d":-19.85}},"last_updated":1534297693}
     * metadata : {"timestamp":1534297232,"error":null}
     */

    private DataBean data;
    private MetadataBean metadata;

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public MetadataBean getMetadata() {
        return metadata;
    }

    public void setMetadata(MetadataBean metadata) {
        this.metadata = metadata;
    }

    public static class DataBean implements Serializable{
        /**
         * id : 42
         * name : Primecoin
         * symbol : XPM
         * website_slug : primecoin
         * rank : 181
         * circulating_supply : 24589301
         * total_supply : 24589301
         * max_supply : null
         * quotes : {"USD":{"price":0.8454120128,"volume_24h":243176.783181443,"market_cap":20788090,"percent_change_1h":-1.61,"percent_change_24h":-2.08,"percent_change_7d":-19.85},"CNY":{"price":5.8238742737,"volume_24h":1675196.2239803215,"market_cap":143204995,"percent_change_1h":-1.61,"percent_change_24h":-2.08,"percent_change_7d":-19.85}}
         * last_updated : 1534297693
         */

        private long id;
        private String name;
        private String symbol;
        private String website_slug;
        private int rank;
        private long circulating_supply;
        private double total_supply;
        private long max_supply;
        private QuotesBean quotes;
        private long last_updated;

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getSymbol() {
            return symbol;
        }

        public void setSymbol(String symbol) {
            this.symbol = symbol;
        }

        public String getWebsite_slug() {
            return website_slug;
        }

        public void setWebsite_slug(String website_slug) {
            this.website_slug = website_slug;
        }

        public int getRank() {
            return rank;
        }

        public void setRank(int rank) {
            this.rank = rank;
        }

        public long getCirculating_supply() {
            return circulating_supply;
        }

        public void setCirculating_supply(long circulating_supply) {
            this.circulating_supply = circulating_supply;
        }

        public double getTotal_supply() {
            return total_supply;
        }

        public void setTotal_supply(double total_supply) {
            this.total_supply = total_supply;
        }

        public long getMax_supply() {
            return max_supply;
        }

        public void setMax_supply(long max_supply) {
            this.max_supply = max_supply;
        }

        public QuotesBean getQuotes() {
            return quotes;
        }

        public void setQuotes(QuotesBean quotes) {
            this.quotes = quotes;
        }

        public long getLast_updated() {
            return last_updated;
        }

        public void setLast_updated(long last_updated) {
            this.last_updated = last_updated;
        }

        public static class QuotesBean implements Serializable{
            /**
             * USD : {"price":0.8454120128,"volume_24h":243176.783181443,"market_cap":20788090,"percent_change_1h":-1.61,"percent_change_24h":-2.08,"percent_change_7d":-19.85}
             * CNY : {"price":5.8238742737,"volume_24h":1675196.2239803215,"market_cap":143204995,"percent_change_1h":-1.61,"percent_change_24h":-2.08,"percent_change_7d":-19.85}
             */

            private USDBean USD;
            private CNYBean CNY;

            public USDBean getUSD() {
                return USD;
            }

            public void setUSD(USDBean USD) {
                this.USD = USD;
            }

            public CNYBean getCNY() {
                return CNY;
            }

            public void setCNY(CNYBean CNY) {
                this.CNY = CNY;
            }

            public static class USDBean implements Serializable{
                /**
                 * price : 0.8454120128
                 * volume_24h : 243176.783181443
                 * market_cap : 20788090
                 * percent_change_1h : -1.61
                 * percent_change_24h : -2.08
                 * percent_change_7d : -19.85
                 */

                private float price;
                private double volume_24h;
                private int market_cap;
                private double percent_change_1h;
                private double percent_change_24h;
                private double percent_change_7d;

                public float getPrice() {
                    return price;
                }

                public void setPrice(float price) {
                    this.price = price;
                }

                public double getVolume_24h() {
                    return volume_24h;
                }

                public void setVolume_24h(double volume_24h) {
                    this.volume_24h = volume_24h;
                }

                public int getMarket_cap() {
                    return market_cap;
                }

                public void setMarket_cap(int market_cap) {
                    this.market_cap = market_cap;
                }

                public double getPercent_change_1h() {
                    return percent_change_1h;
                }

                public void setPercent_change_1h(double percent_change_1h) {
                    this.percent_change_1h = percent_change_1h;
                }

                public double getPercent_change_24h() {
                    return percent_change_24h;
                }

                public void setPercent_change_24h(double percent_change_24h) {
                    this.percent_change_24h = percent_change_24h;
                }

                public double getPercent_change_7d() {
                    return percent_change_7d;
                }

                public void setPercent_change_7d(double percent_change_7d) {
                    this.percent_change_7d = percent_change_7d;
                }
            }

            public static class CNYBean implements Serializable{
                /**
                 * price : 5.8238742737
                 * volume_24h : 1675196.2239803215
                 * market_cap : 143204995
                 * percent_change_1h : -1.61
                 * percent_change_24h : -2.08
                 * percent_change_7d : -19.85
                 */

                private float price;
                private double volume_24h;
                private int market_cap;
                private double percent_change_1h;
                private double percent_change_24h;
                private double percent_change_7d;

                public float getPrice() {
                    return price;
                }

                public void setPrice(float price) {
                    this.price = price;
                }

                public double getVolume_24h() {
                    return volume_24h;
                }

                public void setVolume_24h(double volume_24h) {
                    this.volume_24h = volume_24h;
                }

                public int getMarket_cap() {
                    return market_cap;
                }

                public void setMarket_cap(int market_cap) {
                    this.market_cap = market_cap;
                }

                public double getPercent_change_1h() {
                    return percent_change_1h;
                }

                public void setPercent_change_1h(double percent_change_1h) {
                    this.percent_change_1h = percent_change_1h;
                }

                public double getPercent_change_24h() {
                    return percent_change_24h;
                }

                public void setPercent_change_24h(double percent_change_24h) {
                    this.percent_change_24h = percent_change_24h;
                }

                public double getPercent_change_7d() {
                    return percent_change_7d;
                }

                public void setPercent_change_7d(double percent_change_7d) {
                    this.percent_change_7d = percent_change_7d;
                }
            }
        }
    }

    public static class MetadataBean implements Serializable{
        /**
         * timestamp : 1534297232
         * error : null
         */

        private int timestamp;
        private Object error;

        public int getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(int timestamp) {
            this.timestamp = timestamp;
        }

        public Object getError() {
            return error;
        }

        public void setError(Object error) {
            this.error = error;
        }
    }
}
