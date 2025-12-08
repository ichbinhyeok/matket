package org.example.matket.domain.service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.example.matket.domain.entity.Comment;
import org.example.matket.domain.entity.Order;
import org.example.matket.domain.entity.Post;
import org.example.matket.domain.entity.Product;
import org.example.matket.domain.enums.CommentIntent;
import org.example.matket.domain.enums.OrderStatus;
import org.example.matket.domain.notification.NotificationService;
import org.example.matket.domain.repository.CommentRepository;
import org.example.matket.domain.repository.OrderRepository;
import org.example.matket.domain.repository.PostRepository;
import org.example.matket.domain.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderProcessingService {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final NotificationService notificationService;

    private final Map<Long, Object> productLocks = new ConcurrentHashMap<>();

    @Transactional
    public Optional<Order> handleIncomingComment(CommentPayload payload) {
        Comment comment = commentRepository.findByBandCommentId(payload.bandCommentId())
                .orElseGet(() -> persistComment(payload));

        if (comment.getIntent() != CommentIntent.ORDER) {
            return Optional.empty();
        }

        Optional<OrderRequest> orderRequest = parseOrderRequest(comment);
        if (orderRequest.isEmpty()) {
            return Optional.empty();
        }

        OrderRequest request = orderRequest.get();
        return reserveStock(comment, request.productName(), request.quantity());
    }

    protected Comment persistComment(CommentPayload payload) {
        Post post = postRepository.findByBandPostId(payload.bandPostId())
                .orElseThrow(() -> new IllegalArgumentException("Unknown post for incoming comment"));

        Comment comment = Comment.builder()
                .post(post)
                .bandCommentId(payload.bandCommentId())
                .commenterName(payload.commenterName())
                .content(payload.content())
                .intent(classifyIntent(payload.content()))
                .processed(false)
                .commentedAt(payload.commentedAt())
                .build();
        return commentRepository.save(comment);
    }

    /**
     * Stub: replace with LLM-based intent classifier.
     */
    protected CommentIntent classifyIntent(String content) {
        return CommentIntent.UNKNOWN;
    }

    /**
     * Stub: replace with structured parsing of quantity and product name from comment text.
     */
    protected Optional<OrderRequest> parseOrderRequest(Comment comment) {
        return Optional.empty();
    }

    @Transactional
    protected Optional<Order> reserveStock(Comment comment, String productName, int quantity) {
        Optional<Product> productOptional = productRepository.findByName(productName);
        if (productOptional.isEmpty()) {
            return Optional.empty();
        }

        Product product = productOptional.get();
        Object lock = productLocks.computeIfAbsent(product.getId(), key -> new Object());

        synchronized (lock) {
            Product managedProduct = productRepository.findById(product.getId())
                    .orElseThrow(() -> new IllegalStateException("Product disappeared during reservation"));

            if (managedProduct.getRemainingStock() != null) {
                if (managedProduct.getRemainingStock() < quantity) {
                    notificationService.notifyOwner("Insufficient stock for product: " + managedProduct.getName());
                    return Optional.empty();
                }
                managedProduct.setRemainingStock(managedProduct.getRemainingStock() - quantity);
            }

            Order order = Order.builder()
                    .product(managedProduct)
                    .comment(comment)
                    .quantity(quantity)
                    .status(OrderStatus.PENDING)
                    .reservedAt(java.time.LocalDateTime.now())
                    .build();

            Order savedOrder = orderRepository.save(order);
            comment.setProcessed(true);
            return Optional.of(savedOrder);
        }
    }

    public record CommentPayload(String bandCommentId, String bandPostId, String commenterName, String content,
                                 java.time.LocalDateTime commentedAt) {
    }

    protected record OrderRequest(String productName, int quantity) {
    }
}
