package xiaozhi.common.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TreeUtilsTest {

    /** Minimal TreeNode subclass used purely for test fixtures. */
    public static class FakeNode extends TreeNode<FakeNode> {
        public FakeNode(long id, Long pid) {
            setId(id);
            setPid(pid);
            setChildren(new ArrayList<>());
        }
    }

    private FakeNode node(long id, Long pid) {
        return new FakeNode(id, pid);
    }

    @Test
    void buildFromPidReturnsOnlyThatBranch() {
        // 1 -> (2 -> (4), 3 -> (5))
        List<FakeNode> flat = Arrays.asList(
                node(1L, 0L),
                node(2L, 1L),
                node(3L, 1L),
                node(4L, 2L),
                node(5L, 3L));

        List<FakeNode> trees = TreeUtils.build(flat, 0L);

        assertEquals(1, trees.size());
        FakeNode root = trees.get(0);
        assertEquals(Long.valueOf(1L), root.getId());
        assertEquals(2, root.getChildren().size());

        // Grandchildren should have been attached recursively.
        for (FakeNode direct : root.getChildren()) {
            assertEquals(1, direct.getChildren().size());
        }
    }

    @Test
    void buildFromNonMatchingPidReturnsEmptyList() {
        List<FakeNode> flat = Arrays.asList(node(1L, 0L), node(2L, 1L));
        List<FakeNode> trees = TreeUtils.build(flat, 999L);
        assertTrue(trees.isEmpty());
    }

    @Test
    void buildInferringRootsAttachesChildrenByParentId() {
        // Root (pid=0 which isn't in the list) + two direct children of root 1.
        List<FakeNode> flat = Arrays.asList(
                node(1L, 0L),
                node(2L, 1L),
                node(3L, 1L),
                node(4L, 2L));

        List<FakeNode> result = TreeUtils.build(flat);

        // Node 1's parent (pid=0) isn't in the map, so node 1 surfaces as a root.
        // Nodes 2 and 3 become children of node 1; node 4 becomes a grandchild via node 2.
        assertEquals(1, result.size());
        FakeNode root = result.get(0);
        assertEquals(Long.valueOf(1L), root.getId());
        assertEquals(2, root.getChildren().size());

        FakeNode two = root.getChildren().stream()
                .filter(n -> n.getId().equals(2L)).findFirst().orElseThrow();
        assertEquals(1, two.getChildren().size());
        assertEquals(Long.valueOf(4L), two.getChildren().get(0).getId());
    }

    @Test
    void buildWithEmptyInputReturnsEmptyList() {
        assertTrue(TreeUtils.build(new ArrayList<FakeNode>()).isEmpty());
    }
}
