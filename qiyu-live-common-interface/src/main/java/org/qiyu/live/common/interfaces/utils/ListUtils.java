package org.qiyu.live.common.interfaces.utils;

import java.util.ArrayList;
import java.util.List;

public class ListUtils {

    /**
     * 将一个大的List集合拆解成多个小List集合
     * @param list
     * @param subNum
     * @param <T>
     * @return
     */
    public static <T> List<List<T>> splistList(List<T> list, int subNum) {
        List<List<T>> resultList = new ArrayList<>();
        int priIndex = 0;
        int lastIndex = 0;
        int insertTime = list.size() / subNum;
        List<T> subList;
        for (int i = 0; i <= insertTime; i++) {
            priIndex = subNum * i;
            lastIndex = priIndex + subNum;
            if (i != insertTime) {
                subList = list.subList(priIndex, lastIndex);
            } else {
                subList = list.subList(priIndex, list.size());
            }
            if (subList.size() > 0) {
                resultList.add(subList);
            }
        }
        return resultList;
    }
}