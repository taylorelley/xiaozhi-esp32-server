package xiaozhi.common.utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

/**
 * 树节点，allneedimplement树节点 ，都need继承该class
 * Copyright (c) 人人开source All rights reserved.
 * Website: https://www.renren.io
 */
@Data
public class TreeNode<T> implements Serializable {

    /**
     * Primary key
     */
    private Long id;
    /**
     * 上级ID
     */
    private Long pid;
    /**
     * 子节点list
     */
    private List<T> children = new ArrayList<>();

}