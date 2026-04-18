package xiaozhi.common.utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConvertUtilsTest {

    public static class Source {
        private String name;
        private int age;
        private String unrelated;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }
        public String getUnrelated() { return unrelated; }
        public void setUnrelated(String unrelated) { this.unrelated = unrelated; }
    }

    public static class Target {
        private String name;
        private int age;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }
    }

    private Source makeSource(String name, int age) {
        Source s = new Source();
        s.setName(name);
        s.setAge(age);
        s.setUnrelated("ignored");
        return s;
    }

    @Test
    void copiesMatchingPropertiesFromObject() {
        Target t = ConvertUtils.sourceToTarget(makeSource("alice", 30), Target.class);
        assertNotNull(t);
        assertEquals("alice", t.getName());
        assertEquals(30, t.getAge());
    }

    @Test
    void returnsNullWhenSourceObjectIsNull() {
        assertNull(ConvertUtils.sourceToTarget((Object) null, Target.class));
    }

    @Test
    void returnsNullWhenSourceCollectionIsNull() {
        assertNull(ConvertUtils.sourceToTarget((List<?>) null, Target.class));
    }

    @Test
    void emptyCollectionProducesEmptyList() {
        List<Target> targets = ConvertUtils.sourceToTarget(Collections.emptyList(), Target.class);
        assertNotNull(targets);
        assertTrue(targets.isEmpty());
    }

    @Test
    void copiesMatchingPropertiesFromCollection() {
        List<Source> sources = Arrays.asList(
                makeSource("alice", 30),
                makeSource("bob", 25),
                makeSource("carol", 40));

        List<Target> targets = ConvertUtils.sourceToTarget(sources, Target.class);

        assertNotNull(targets);
        assertEquals(3, targets.size());
        assertEquals("alice", targets.get(0).getName());
        assertEquals(30, targets.get(0).getAge());
        assertEquals("bob", targets.get(1).getName());
        assertEquals("carol", targets.get(2).getName());
    }

    @Test
    void ignoresPropertiesThatDoNotExistOnTarget() {
        // 'unrelated' does not exist on Target, which must silently skip rather than throw.
        Source s = makeSource("x", 1);
        Target t = ConvertUtils.sourceToTarget(s, Target.class);
        assertNotNull(t);
        assertEquals("x", t.getName());
    }
}
