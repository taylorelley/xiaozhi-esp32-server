package xiaozhi.common.page;

import java.io.Serializable;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * paginationtoolclass
 * Copyright (c) 人人开source All rights reserved.
 * Website: https://www.renren.io
 */
@Data
@Schema(description = "paginationdata")
public class PageData<T> implements Serializable {
    @Schema(description = "totalrecordnumber")
    private int total;

    @Schema(description = "listdata")
    private List<T> list;

    /**
     * pagination
     *
     * @param list  listdata
     * @param total totalrecordnumber
     */
    public PageData(List<T> list, long total) {
        this.list = list;
        this.total = (int) total;
    }
}