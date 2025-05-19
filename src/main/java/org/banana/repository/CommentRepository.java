package org.banana.repository;

import org.banana.dto.comment.CommentResponseDto;
import org.banana.entity.Comment;
import org.banana.repository.crud.CrudRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CommentRepository extends CrudRepository<Comment, UUID> {

    /**
     * Возвращает комментарий с комментатором
     *
     * @param id идентификатор комментария
     * @return Optional<Comment> пустой Optional если комментария не существует
     */
    Optional<Comment> findFetchedById(UUID id);

    /**
     * Возвращает список комментариев к объявлению у которых rootCommentId = null (то есть они рутовые)
     *
     * @param advertisementId идентификатор объявления
     * @param offset          смещение для пагинации
     * @param limit           максимальное количество комментариев которое нужно вернуть
     * @return List<CommentResponseDto>
     */
    List<CommentResponseDto> findAllRootCommentsByAdvertisementId(UUID advertisementId, int offset, int limit);

    /**
     * Возвращает список комментариев у которых rootCommentId входит в переданный список
     *
     * @param rootCommentIds список идентификаторов рутовых комментариев
     * @return List<CommentResponseDto>
     */
    List<CommentResponseDto> findAllCommentsInRootIds(List<UUID> rootCommentIds);
}
