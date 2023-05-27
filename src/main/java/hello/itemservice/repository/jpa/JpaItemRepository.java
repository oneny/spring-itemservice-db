package hello.itemservice.repository.jpa;

import hello.itemservice.domain.Item;
import hello.itemservice.repository.ItemRepository;
import hello.itemservice.repository.ItemSearchCond;
import hello.itemservice.repository.ItemUpdateDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
@Transactional
public class JpaItemRepository implements ItemRepository {

  /**
   * 생성자를 보면 스프링을 통해 엔티티 매니저(EntityManager)라는 것을 주입받은 것을 확인할 수 있다.
   * JPA의 모든 동작은 엔티티 매니저를 통해서 이루어진다.
   * 엔티티 매니저는 내부에 데이터소스를 가지고 있고, 데이터베이스에 접근할 수 있다.
   */
  private final EntityManager em;

  public JpaItemRepository(EntityManager em) {
    this.em = em;
  }

  @Override
  public Item save(Item item) {
    em.persist(item);
    return item;
  }

  @Override
  public void update(Long itemId, ItemUpdateDto updateParam) {
    Item findItem = em.find(Item.class, itemId);
    findItem.setItemName(updateParam.getItemName());
    findItem.setPrice(updateParam.getPrice());
    findItem.setQuantity(updateParam.getQuantity());
  }

  @Override
  public Optional<Item> findById(Long id) {
    Item item = em.find(Item.class, id);
    return Optional.ofNullable(item);
  }

  @Override
  public List<Item> findAll(ItemSearchCond cond) {
    // JPQL은 엔티티 객체를 대상으로 SQL을 실행한다.
    // 엔티티 객체를 대상으로 하기 때문에 from 다음에 Item 엔티티 객체 이름이 들어간다.
    // 엔티티 객체와 속성의 대소문자는 구분해야 한다.
    // i는 이때 Item 엔티티 자체를 말한다.
    String jpql = "select i from Item i";

    Integer maxPrice = cond.getMaxPrice();
    String itemName = cond.getItemName();

    if (StringUtils.hasText(itemName) || maxPrice != null) {
      jpql += " where";
    }

    boolean andFlag = false;
    if (StringUtils.hasText(itemName)) {
      jpql += " i.itemName like concat('%',:itemName,'%')";
      andFlag = true;
    }

    if (maxPrice != null) {
      if (andFlag) {
        jpql += " and";
      }

      jpql += " i.price <= :maxPrice";
    }

    log.info("jpql={}", jpql);

    TypedQuery<Item> query = em.createQuery(jpql, Item.class);
    if (StringUtils.hasText(itemName)) {
      query.setParameter("itemName", itemName);
    }
    if (maxPrice != null) {
      query.setParameter("maxPrice", maxPrice);
    }

    return query.getResultList();
  }
}
