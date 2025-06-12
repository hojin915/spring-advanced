# SPRING ADVANCED
### 트러블슈팅  

#### 1. TodoSaveRequest(Todo Save Request DTO) validation 수정

1-1 문제인식  
- TodoSaveRequest 의 필드인 `title`, `contents`를 보면 기존에는 `@NotBlank` 어노테이션이 적용되어 있었다.  
Schedule 역할을 하는 Todo 를 생성할 때 띄어쓰기가 불가능한 조건은 적합하지 않아 보인다.  

1-2 해결 방안  
- 검증 조건을 변경  
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

#### 2. Manager 등록 예외처리 추가

2-1 문제인식
- Todo 에 담당자를 지정하는 비즈니스 로직인 `/domain/manager/service/ManagerService` 에 위치한 saveManager 메서드에서  
담당자를 등록하는 유저가 일정을 작성한 유저와 동일한 유저인지 확인하는 절차가 없다.  
따라서 다른 사람이 만든 일정의 담당자 권한을 누구든지 부여할 수 있는 상황이기 때문에 추가적인 예외처리가 필요했다. 

2-2 해결 방안
- 본인을 확인하는 예외처리를 추가
```java
// ManagerService.java 내부의 saveManager() 에 추가한 예외처리
  if(!user.getId().equals(todo.getUser().getId())){
    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "본인이 작성하지 않은 Todo 에 담당자를 등록할 수 없습니다.");
  }
```

2-3 해결완료  
- 본인이 작성하지 않은 일정에 대한 담당자 등록 했을 때
```json
{
    "timestamp": "2025-06-12T03:23:32.313+00:00",
    "status": 401,
    "error": "Unauthorized",
    "path": "/todos/12/managers"
}
```
권한에 관련된 부분이 `UserRole.ADMIN`, `UserRole.USER` 이렇게만 관리되고 있는데,
그렇기 때문에 관련된 커스텀 예외가 존재하지 않는 것으로 보인다.  
Admin, User 역할 뿐만 아니라 본인 확인과 같은 검증이 커스텀 예외와 함께 추가되어야 할 것으로 보인다. 