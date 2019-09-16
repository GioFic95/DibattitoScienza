begin TRANSACTION;


DROP TABLE IF EXISTS tweet;

CREATE TABLE tweet (
  id bigint NOT NULL,
  user_id bigint NOT NULL,
  lang char(10) NOT NULL,
  in_reply_id bigint DEFAULT NULL,
  retweet_id bigint DEFAULT NULL,
  datetime timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  text text NOT NULL,
  PRIMARY KEY (id)
);

CREATE INDEX user_id ON tweet ( user_id );
CREATE INDEX datetime ON tweet ( datetime );
CREATE INDEX lang ON tweet ( lang );
CREATE INDEX in_reply_id ON tweet ( in_reply_id );
CREATE INDEX retweet_id ON tweet ( retweet_id );


DROP TABLE IF EXISTS hashtag_tweet;

CREATE TABLE hashtag_tweet (
  tweet_id bigint NOT NULL,
  hashtag_text text NOT NULL,
  PRIMARY KEY (hashtag_text, tweet_id),
  FOREIGN KEY (tweet_id) REFERENCES tweet (id)
);

CREATE INDEX hashtag_text ON hashtag_tweet ( hashtag_text );
CREATE INDEX tweet_id ON hashtag_tweet ( tweet_id );

COMMIT;