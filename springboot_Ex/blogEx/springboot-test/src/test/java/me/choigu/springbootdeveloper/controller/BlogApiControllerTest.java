package me.choigu.springbootdeveloper.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.choigu.springbootdeveloper.domain.Article;
import me.choigu.springbootdeveloper.dto.AddArticleRequest;
import me.choigu.springbootdeveloper.dto.ArticleResponse;
import me.choigu.springbootdeveloper.dto.UpdateArticleRequest;
import me.choigu.springbootdeveloper.repository.BlogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;


@SpringBootTest
@AutoConfigureMockMvc
class BlogApiControllerTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    BlogRepository blogRepository;

    @BeforeEach
    public void mockMvcSetup(){
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .build();
        blogRepository.deleteAll();
    }

    @DisplayName("addArticle: 블로그 글 추가에 성공한다.")
    @Test
    public void addArticle() throws Exception{

    // given : 블로그 글 추가에 필요한 요청 객체 생성
        final String url = "/api/articles";
        final String title = "title";
        final String content = "content";
        final AddArticleRequest userRequest = new AddArticleRequest(title, content);

        final String requestBody = objectMapper.writeValueAsString(userRequest);

    // when : 블로그 글 추가 api에 요청을 보냄. 이때 요청 타입은 JSON이며, given절에서 미리 만들어둔 객체를 요청 본문으로 함께 보냄.
        ResultActions result = mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(requestBody));

    // then : 응답코드가 201 Created인지 확인. Blog를 전체 조회해 크기가 1인지 확인하고, 실제로 저장된 데이터와 요청 데이터가 일치한지 확인.
        result.andExpect(status().isCreated());

        List<Article> articles = blogRepository.findAll();

        assertThat(articles.size()).isEqualTo(1);
        assertThat(articles.get(0).getTitle()).isEqualTo(title);
        assertThat(articles.get(0).getContent()).isEqualTo(content);



    }

    @DisplayName("findAllArticles: 블로그 글 목록 조회에 성공한다.")
    @Test
    public void findAllArticles() throws Exception {

    // given : 블로그 글을 저장
        final String url = "/api/articles";
        final String title = "title";
        final String content = "content";

        blogRepository.save(Article.builder()
                .title(title)
                .content(content)
                .build());

    // when : 목록 조회 api를 호출
        final ResultActions resultActions = mockMvc.perform(get(url)
                .accept(MediaType.APPLICATION_JSON));

    // then : 저장된 글 수와 조회한 글수가 같은지, 조회된 0번째 값이 요청한 것과 같은지 확인
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].content").value(content))
                .andExpect(jsonPath("$[0].title").value(title));
    }

    @DisplayName("findArticle: 블로그 글 조회에 성공한다.")
    @Test
    public void findArticle() throws Exception{
    // given : 블로그 글 저장
        final String url = "/api/articles/{id}";
        final String title = "test1";
        final String content = "test2";

        Article savedArticle = blogRepository.save(Article.builder()
                .title(title)
                .content(content)
                .build()
        );

    // when : 저장한 글의 id 값으로 api 호출
        final ResultActions resultActions = mockMvc.perform(get(url, savedArticle.getId()));

    // then : 응답 코드가 200 ok이고, 반환받은 content와 title이 저장된 값과 같은지 확인
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value(content))
                .andExpect(jsonPath("$.title").value(title));
    }

    @DisplayName("deleteArticle: 블로그 글 삭제에 성공한다.")
    @Test
    public void deleteArticle() throws Exception{
    // given : 블로그 글을 저장한다.
        final String url = "/api/articles/{id}";
        final String title = "dTitle1";
        final String content = "dContent1";

        Article savedArticle = blogRepository.save(Article.builder()
                .title(title)
                .content(content)
                .build());

    // when : 저장된 글의 id값으로 삭제 api를 호출한다.
        mockMvc.perform(delete(url, savedArticle.getId()))
                .andExpect(status().isOk());

    // then : 응답코드가 200ok이고, 블로그 글 리스트 전체 조회해 조회한 배열의 크기가 0인지 확인한다.
        List<Article> articles = blogRepository.findAll();
        assertThat(articles).isEmpty();
    }

    @DisplayName("updateArticle: 블로그 글 수정에 성공한다.")
    @Test
    public void updateArticle() throws Exception{
    // given : 블로그 글을 저장하고 글 수정에 필요한 요청 객체를 만든다.
        final String url = "/api/articles/{id}";
        final String title = "title";
        final String content = "content";
        Article savedArticle = blogRepository.save(Article.builder()
                .title(title)
                .content(content)
                .build());

        final String newTitle = "new title";
        final String newContent = "new content";

        UpdateArticleRequest request = new UpdateArticleRequest(newTitle, newContent);

    // when : update api로 수정 요청을 보냄. 이때 요청 타입은 JSON이며, given절에서 미리 만들어둔 객체를 요청 본문으로 함께 보낸다.
        ResultActions resultActions = mockMvc.perform(put(url, savedArticle.getId())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(request)));

    // then : 응답코드가 200ok인지 확인. 블로그 글 id로 조회한 후 값이 수정되었는지 확인
        resultActions.andExpect(status().isOk());

        Article article = blogRepository.findById(savedArticle.getId()).get();

        assertThat(article.getTitle()).isEqualTo(newTitle);
        assertThat(article.getContent()).isEqualTo(newContent);
    }


}