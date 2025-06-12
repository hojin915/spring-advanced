# SPRING ADVANCED
### 트러블슈팅  

#### 1.TodoSaveRequest(Todo Save Request DTO) validation 수정

1-1 문제인식  
- TodoSaveRequest 의 필드인 `title`, `contents`를 보면 기존에는 `@NotBlank` 어노테이션이 적용되어 있었다.  
Schedule 역할을 하는 Todo 를 생성할 때 띄어쓰기가 불가능한 조건은 적합하지 않아 보인다.  

1-2 해결 방안  
- 검증 조건을 변경해준다.  
  - `@NotNull` 어노테이션으로 Todos 테이블에 title, contents 필드값이 null 로 들어가는 경우를 방지한다.  
  - 추가적으로 `@Size(min = 1, max = 255)` 어노테이션을 추가해서
  현재 Todo Entity 설정에서 String 의 기본 설정으로 DB 에서는 title, contents 각각 varchar(255)
  타입으로 받도록 적용되어 있는데, 길이에 대한 제한이 없어 255자를 넘어가는 요청에 대해 생길 수 있는 에러를 방지한다  

1-3 해결완료
```json
{
    "id": 17,
    "title": "제목 띄어쓰기",
    "contents": "내용 띄어쓰기",
    "weather": "Hot and Humid",
    "user": {
        "id": 3,
        "email": "hojin@naver.com"
    }
}
```
동일한 이유로 Comment Entity 의 contents 필드 또한 validation 을 수정해 주었다