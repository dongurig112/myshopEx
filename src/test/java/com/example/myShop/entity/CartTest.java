package com.example.myShop.entity;

import com.example.myShop.dto.MemberFormDto;
import com.example.myShop.repository.CartRepository;
import com.example.myShop.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import javax.persistence.EntityNotFoundException;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@Transactional
@TestPropertySource(locations="classpath:application-test.properties")
class CartTest {
   @Autowired
   CartRepository cartRepository;

   @Autowired
   MemberRepository memberRepository;

   @Autowired
   PasswordEncoder passwordEncoder;

   @PersistenceContext  //엔티티, 영속성 관리해주는 애,,? 1차 캐시 저장해놓고 관리해주는 애?/
   EntityManager em;

    public Member createMember(){
        MemberFormDto memberFormDto = new MemberFormDto();
        memberFormDto.setEmail("choco@email.com");
        memberFormDto.setName("연초코");
        memberFormDto.setAddress("서울시 성동구 응봉동");
        memberFormDto.setPassword("1234");
        return Member.createMember(memberFormDto, passwordEncoder);
    }

    @Test
    @DisplayName("장바구니 회원 엔티티 매핑 조회 테스트")
    public void findCartAndMemberTest(){
        Member member = createMember();
        memberRepository.save(member);
        Cart cart = new Cart();
        cart.setMember(member);
        cartRepository.save(cart);
        em.flush();
        em.clear();
        Cart savedCart = cartRepository.findById(cart.getId())
                .orElseThrow(EntityNotFoundException::new);
        assertEquals(savedCart.getMember().getId(), member.getId());
    }

}