---
project_name: utils.shortenurl
user_name: TT
date: '2026-03-28'
sections_completed:
  - technology_stack
  - language_rules
  - framework_rules
  - testing_rules
  - quality_rules
  - workflow_rules
  - anti_patterns
status: complete
rule_count: 32
optimized_for_llm: true
---

# Project Context for AI Agents

_File này gom các quy tắc và pattern mà agent **phải** tuân thủ khi sửa code. Ưu tiên chi tiết **không hiển nhiên** — tránh lặp lại kiến thức Java/Spring phổ biến._

---

## Technology Stack & Versions

| Thành phần | Phiên bản / ghi chú |
|------------|---------------------|
| Java | 21 (Gradle toolchain) |
| Spring Boot | 4.0.0 |
| Spring Dependency Management | 1.1.7 |
| GraalVM Native Build Tools (plugin) | 0.11.3 |
| Build | Gradle (dự án dùng `build.gradle`; JAR thường đã tắt, chỉ boot JAR) |
| Web | `spring-boot-starter-webmvc` |
| Persistence | Spring Data JPA + MySQL (`mysql-connector-j` runtime) |
| Cache / Redis | `spring-boot-starter-data-redis`, **Redisson 3.27.2** |
| ID | Snowflake (`com.relops:snowflake:1.1`), NanoID (`jnanoid:2.0.0`) |
| Validation / JSON / Actuator | starters tương ứng Boot 4 |
| Metrics | Micrometer Prometheus registry |
| Lombok | `compileOnly` + `annotationProcessor` |
| Test | JUnit Platform; `spring-boot-starter-*-test`; H2 test runtime |

**Ràng buộc tương thích:** Giữ đồng bộ phiên bản starters với BOM Spring Boot 4; đổi major Redis/JPA driver cần kiểm tra serialization và kết nối pool.

---

## Critical Implementation Rules

### Language-Specific Rules (Java)

- Dùng **Java 21** và API phù hợp; không hạ cấp ngôn ngữ ẩn trong code mới.
- **Lombok:** chỉ dùng khi codebase đã dùng pattern đó; không tự ý thêm Lombok vào layer chưa có.
- Ưu tiên **rõ ràng** trên hot path (create/redirect): tên biến và nhánh lỗi dễ trace.
- Bất đồng bộ / executor: nếu chạm flush batch hoặc ghi sau, ghi rõ **tính nhất quán** (sync vs eventual) trong thay đổi.

### Framework-Specific Rules (Spring Boot)

- **MySQL / JPA là nguồn sự thật bền** cho alias; Redis, LRU, Bloom chỉ tối ưu.
- **Bloom filter:** không coi là chân lý; false positive được phép, không được gây false negative logic sai.
- **Key generation:** giữ khả năng thay strategy qua abstraction hiện có (`KeyGenerationService`); `@Primary` hiện tại là Snowflake — thay đổi phải đánh giá collision và định dạng URL.
- Controller **mỏng**; orchestration trong service (`ShortenUrlService` / impl).
- Actuator/Prometheus: giữ hoặc cải thiện khả năng quan sát khi đụng latency/error path.

### Testing Rules

- Sau thay đổi: chạy `./gradlew test` (hoặc test có liên quan).
- **Create:** kiểm tra contract response, alias tạo ra, persistence.
- **Redirect:** cả **hit** và **miss** (Bloom/cache có thể che giấu lỗi nếu test sai tầng).
- Test infra: H2 cho test runtime — không giả định behavior MySQL-specific mà không có test/integration tương ứng.
- Repo hiện có ít test; thêm regression khi sửa bug cache/duplicate/delayed write.

### Code Quality & Style Rules

- **Package:** `controller` → HTTP; `service` / `service/impl` → use case; `repository` → JPA; `entity`, `dto`, `config` đúng vai trò (theo `CONVENTIONS.md`).
- Không trộn **refactor rộng** với sửa chức năng trên đường nóng trong cùng một PR/commit nếu tránh được.
- Mọi thay đổi **schema / entity** phải ghi chú tác động dữ liệu.
- Không hardcode secret môi trường vào file commit.

### Development Workflow Rules

- Ưu tiên nhánh/feature riêng khi thay đổi lớn; working tree bẩn vẫn có thể dev local nhưng nên tách PR theo mục tiêu.
- Cập nhật doc (`ARCHITECTURE.md`, `README`) khi luồng create/persistence thay đổi thực sự — **đọc code** nếu mâu thuẫn với doc.

### Critical Don't-Miss Rules

- **Không** phá contract `POST /app/api/create` và semantics `GET /{alias}` mà không ghi rõ breaking change.
- **Không** triển khai nửa vời **custom alias**: phải khớp DTO, service, uniqueness, doc cùng lúc (`CONSTRAINTS.md`).
- **Không** giả định `request.alias` đang hoạt động đầy đủ trong luồng create chính.
- Khi đụng **ghi DB / buffer / micro-batch**: xử lý như vùng **correctness-sensitive**; nêu rõ client nhận response khi nào so với durability.
- Doc kiến trúc có thể còn đề cập write-behind cũ; **luôn xác minh** file Java hiện tại (đường ghi alias thực tế).
- Tham chiếu thứ tự đọc sâu trong repo: `AGENTS_CONTEXT_INDEX.md` → `PROJECT_OVERVIEW.md` → `ARCHITECTURE.md` → `CONSTRAINTS.md` → `CONVENTIONS.md`.

---

## Usage Guidelines

**Cho AI agents**

- Đọc file này trước khi implement; khi mơ hồ, chọn hướng **an toàn hơn** (ít surprise về consistency/API).
- Cập nhật file này khi stack hoặc luồng nghiệp vụ đổi có hệ quả cho agent.

**Cho người**

- Giữ file gọn; xóa quy tắc đã thành hiển nhiên sau thời gian dài.
- Khi nâng Boot/Java/driver: sửa bảng phiên bản ở trên.

_Lưu trữ tại `docs/` để khớp `project_knowledge` trong `_bmad/bmm/config.yaml`._

_Last updated: 2026-03-28_
