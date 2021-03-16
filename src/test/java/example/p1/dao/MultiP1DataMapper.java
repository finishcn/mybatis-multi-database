package example.p1.dao;

import org.apache.ibatis.annotations.Mapper;

/**
 * Mapper鍜孹ML绫诲悕闇�瑕佷互Multi涓哄墠缂�
 * 涓嶅甫Multi鍓嶇紑鐨凪apper琚涓烘槸鏅�歁apper锛屼笉杩涜澶氭暟鎹簱瀹炰緥閰嶇疆
 */
@Mapper
public interface MultiP1DataMapper {

    String name = "P1DataMapper";

    String getById(Integer id);
}
