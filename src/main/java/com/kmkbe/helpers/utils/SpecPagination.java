package com.kmkbe.helpers.utils;

import com.kmkbe.core.domain.model.PaginationResult;
import com.kmkbe.core.domain.request.PaginationRequest;
import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class SpecPagination<D, R> {
    List<D> privData ;
    PaginationRequest request;
    public SpecPagination(List<D> data){
        this.privData = data;
    }

    public SpecPagination(Page<D> data, PaginationRequest request){
        this.privData = data.stream().toList();
        this.request = request;
    }
    public SpecPagination(List<D> data, PaginationRequest request){
        this.privData = data;
        this.request = request;
    }

    public SpecPagination(Optional<List<D>> data, PaginationRequest request){
        data.ifPresent(ds -> this.privData = ds);
        this.request = request;
    }
    public boolean isSearchBy(String nameBy){
        return getSearchBy().equalsIgnoreCase(nameBy);
    }
    public String getSearchBy(){
        return Utils.valueOf(this.request!=null?this.request.getSearchBy():"");
    }
    public String getSearchValue(){
        return Utils.valueOf(this.request!=null?this.request.getSearchValue():"");
    }
    public boolean isSearchUsed(){
        if (this.request!=null){
            return this.request.getSearchBy()!=null && this.request.getSearchValue()!=null;
        }
        return false;
    }
    public static <D, R> PaginationResult<R> paginationData( SpecPagination<D, R> specPagination){
        int pageNo = 0, pageSize = 10;//def

        if (specPagination.request!=null &&  specPagination.request.getPageNo() != null) {
            pageNo = specPagination.request.getPageNo();
        }

        if (specPagination.request!=null &&  specPagination.request.getPageSize() != null) {
            pageSize = specPagination.request.getPageSize();
        }
        if (pageNo > 0) {
            pageNo = pageNo - 1;
        }
        return  paginationData(pageNo, pageSize, specPagination);
    }
    public static <D, R> PaginationResult<R> paginationData(int page, int size, SpecPagination<D, R> specPagination){
        List<R> specData = new ArrayList<>();
        boolean filteredSearch  = specPagination.isSearchUsed();
        for (D datum : specPagination.privData) {
            if (filteredSearch){
                datum = specPagination.search(datum);
            }
            if (datum!=null){
                R r = specPagination.eval(datum);
                if (r!=null && filteredSearch){
                    r = specPagination.filter(r);
                }
                if (r != null) specData.add(r);
            }
        }
        /// pagination Now
        int pages = specData.size()/size;
        if ( pages * size < specData.size()) pages++;

        //sort
        specPagination.sort(specData);

        //prepare data
        List<R> currData = new ArrayList<>();
        for (int i = (size * page) ; i < Math.min((size * page) + size, specData.size()); i++) {
            currData.add(specData.get(i));
        }

        return PaginationResult.<R>builder()
                .currentPage(page + 1)
                .totalData((long)specData.size())
                .totalPage(pages)
                .list(currData)
                .build();
    }
    public R eval(D data){
        return  null;
    }
    public D search(D data){
        return  data;
    }
    public R filter(R data){
        return  data;
    }
    public void sort(List<R> data){

    }
    public boolean equalDate(Date value){
        //value = dd/mm/yyyy
        return getSearchValue().equalsIgnoreCase(Utils.formatDateView(value) );
    }
    public boolean equalNumber(double value){
        return value == Utils.getDouble(getSearchValue());
    }
    public boolean like(String value){
        return Utils.valueOf(value).toLowerCase().contains(getSearchValue().toLowerCase());
    }
    public boolean equal(String value){
        return getSearchValue().equalsIgnoreCase(value);
    }
}
