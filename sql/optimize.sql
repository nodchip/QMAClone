DROP INDEX index_problem_ngram ON problem;
DROP INDEX index_creater ON problem;
DROP INDEX index_sentence ON problem;
DELETE FROM correct_count WHERE GOOD = 0 AND BAD = 0;
DELETE FROM player_answer WHERE COUNT <= 1;
OPTIMIZE TABLE bbs_response, bbs_thread, chat_log, correct_count, creation_log, ignore_id, limited_ip, limited_user_code, link, page_view, player, player_answer, problem, problem_questionnaire, rating_history, report_problem, theme_mode, theme_mode_score, user;
CREATE FULLTEXT INDEX index_sentence USING NGRAM ON problem (SENTENCE);
CREATE FULLTEXT INDEX index_problem_ngram USING NGRAM ON problem (SENTENCE,ANSWER0,ANSWER1,ANSWER2,ANSWER3,CHOICE0,CHOICE1,CHOICE2,CHOICE3,NOTE);
CREATE FULLTEXT INDEX index_creater USING NGRAM ON problem (CREATER);

war削除
cron止める
tomcat停止
mysql再起動
最適化
tomcat開始
cron開始
warアップロード
