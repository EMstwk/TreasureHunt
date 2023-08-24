package plugin.treasurehunt;

import java.io.InputStream;
import java.util.List;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import plugin.treasurehunt.mapper.PlayerScoreMapper;
import plugin.treasurehunt.mapper.data.PlayerScore;

/**
 * DB接続や、それに付随する登録や更新処理を行うクラスです。
 */
public class PlayerScoreDao {

  private final SqlSessionFactory sqlSessionFactory;
  private final PlayerScoreMapper mapper;

  public PlayerScoreDao() {
    try {
      InputStream inputStream = Resources.getResourceAsStream("mybatis-config.xml");
      this.sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
      SqlSession session = sqlSessionFactory.openSession(true);
      this.mapper = session.getMapper(PlayerScoreMapper.class);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * プレイヤースコアテーブルから一覧でスコア情報を取得します。
   *
   * @return スコア情報の一覧
   */
  public List<PlayerScore> selectList() {
    return mapper.selectList();
  }

  /**
   * プレイヤースコアテーブルにスコア情報を登録します。
   *
   * @param playerScore プレイヤースコア
   */
  public void insert(PlayerScore playerScore) {
    mapper.insert(playerScore);
  }
}
