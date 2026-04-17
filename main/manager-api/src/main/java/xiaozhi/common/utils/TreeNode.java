package xiaozhi.common.utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

/**
 * treenode，allneedimplementtreenode ，allneedinheritthisclass
 * Copyright (c) Renren Opensource All rights reserved.
 * Website: https://www.renren.io
 */
@Data
public class TreeNode<T> implements Serializable {

    /**
     * Primary key
     */
    private Long id;
    /**
     * uplevelID
     */
    private Long pid;
    /**
     * childnodelist
     */
    private List<T> children = new ArrayList<>();

}