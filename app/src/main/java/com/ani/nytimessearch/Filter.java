package com.ani.nytimessearch;

import android.support.annotation.Nullable;

import java.io.Serializable;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

public class Filter implements Serializable {
    private Sort sort;
    @Nullable
    private Calendar beginDate;
    private Set<String> newsDesks;

    public Filter() {
        sort = Sort.RELEVANCE;
        newsDesks = new HashSet<>();
    }

    public Sort getSort() {
        return sort;
    }

    public void setSort(Sort sort) {
        this.sort = sort;
    }

    public Calendar getBeginDate() {
        return beginDate;
    }

    public void setBeginDate(Calendar beginDate) {
        this.beginDate = beginDate;
    }

    public Set<String> getNewsDesks() {
        return newsDesks;
    }

    public void setNewsDesks(Set<String> newsDesks) {
        this.newsDesks = newsDesks;
    }

    enum Sort {
        OLDEST("oldest"),
        NEWEST("newest"),
        RELEVANCE("relevance");

        private String value;

        public String getValue() {
            return value;
        }

        Sort(String value) {
            this.value = value;
        }
    }
}
