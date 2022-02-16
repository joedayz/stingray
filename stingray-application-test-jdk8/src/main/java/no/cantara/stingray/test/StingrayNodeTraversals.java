package no.cantara.stingray.test;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class StingrayNodeTraversals {

    public static void depthFirstPostOrderWithConcurrentSiblingExecutions(ExecutorService executor, Set<StingrayNode> ancestors, StingrayNode node, Consumer<StingrayNode> visitor) {
        boolean added = ancestors.add(node);
        if (!added) {
            throw new RuntimeException(String.format("Circular reference. Chain: %s->%s",
                    ancestors.stream().map(StingrayNode::getId).collect(Collectors.joining("->")),
                    node.getId()
            ));
        }
        try {
            List<StingrayNode> children = node.getChildren();
            CompletableFuture[] childTasks = new CompletableFuture[children.size()];
            for (int i = 0; i < childTasks.length; i++) {
                StingrayNode child = children.get(i);
                childTasks[i] = CompletableFuture.runAsync(() -> depthFirstPostOrderWithConcurrentSiblingExecutions(executor, ancestors, child, visitor), executor);
            }
            CompletableFuture.allOf(childTasks).join();
        } finally {
            ancestors.remove(node);
        }
        visitor.accept(node);
    }
}
