package waterballsa.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import waterballsa.entity.User.UserRole;

/**
 * 使用者資訊 DTO (Data Transfer Object)
 *
 * <p>向後相容機制 (Backward Compatibility Mechanism):
 *
 * <p>此 record 實作了完整的向後相容策略，讓舊版 API 和新版 API 可以共存：
 *
 * <h3>1. JSON 序列化/反序列化的雙重映射 (@JsonProperty)</h3>
 *
 * <ul>
 *   <li>experience 欄位: 使用 @JsonProperty("experience") 標註
 *   <li>experiencePoints 欄位: 使用 @JsonProperty("experiencePoints") 標註
 *   <li>效果: 當 Jackson 序列化此物件時，兩個欄位會分別映射到 JSON 中的不同鍵
 *   <li>舊 API 可以只讀取 "experience"，新 API 可以讀取 "experiencePoints"
 *   <li>反序列化時，JSON 中的 "experience" 或 "experiencePoints" 都能正確對應到相應欄位
 * </ul>
 *
 * <h3>2. 建構子重載 (Constructor Overloading)</h3>
 *
 * <ul>
 *   <li>Canonical Constructor (6 參數): Record 自動生成，包含所有欄位
 *   <li>完整資料建構子 (5 參數): 提供 id, username, experiencePoints, level, role
 *   <li>向後相容建構子 (3 參數): 提供 id, username, experience (舊版格式)
 * </ul>
 *
 * <h3>3. 建構子委派鏈 (Constructor Delegation Chain)</h3>
 *
 * <ul>
 *   <li>所有建構子最終都委派給 canonical constructor
 *   <li>3 參數建構子將 experience 同時填入 experience 和 experiencePoints
 *   <li>5 參數建構子將 experiencePoints 同時填入 experience 和 experiencePoints
 *   <li>確保資料一致性，避免欄位值不同步
 * </ul>
 *
 * <h3>4. 實際應用範例</h3>
 *
 * <pre>
 * // 舊程式碼仍然可以運作
 * UserInfo oldStyle = new UserInfo(1L, "user1", 100);
 * // 內部: experience=100, experiencePoints=100, level=null, role=null
 *
 * // 新程式碼使用完整欄位
 * UserInfo newStyle = new UserInfo(1L, "user1", 100, 1, UserRole.STUDENT);
 * // 內部: experience=100, experiencePoints=100, level=1, role=STUDENT
 *
 * // JSON 序列化結果
 * // 舊 API: {"id":1, "username":"user1", "experience":100}
 * // 新 API: {"id":1, "username":"user1", "experience":100, "experiencePoints":100, "level":1, "role":"STUDENT"}
 * </pre>
 *
 * <h3>5. 遷移策略</h3>
 *
 * <ul>
 *   <li>階段 1: 保留舊的 3 參數建構子，讓現有程式碼繼續運作
 *   <li>階段 2: 逐步將程式碼改為使用新的 5 參數建構子
 *   <li>階段 3: 當所有程式碼都遷移完成後，可以移除 3 參數建構子
 *   <li>彈性: 可以長期保留 3 參數建構子以支援外部整合或測試程式碼
 * </ul>
 */
public record UserInfo(
    Long id,
    String username,
    // 舊版 API 使用的欄位名稱，映射到 JSON 的 "experience"
    @JsonProperty("experience") Integer experience,
    // 新版 API 使用的欄位名稱，映射到 JSON 的 "experiencePoints"
    @JsonProperty("experiencePoints") Integer experiencePoints,
    Integer level,
    UserRole role) {

  /**
   * 完整資料建構子 (5 參數)
   *
   * <p>用於建立包含所有業務欄位的使用者資訊，但不需要手動指定 experience 欄位
   *
   * <p>委派機制: 將 experiencePoints 同時填入 experience 和 experiencePoints 確保兩個欄位同步，這樣無論舊 API 讀取 experience
   * 還是新 API 讀取 experiencePoints，都能得到正確的值
   *
   * @param id 使用者 ID
   * @param username 使用者名稱
   * @param experiencePoints 經驗值
   * @param level 等級
   * @param role 角色
   */
  public UserInfo(
      Long id, String username, Integer experiencePoints, Integer level, UserRole role) {
    this(id, username, experiencePoints, experiencePoints, level, role);
  }

  /**
   * 向後相容建構子 (3 參數)
   *
   * <p>此建構子提供與舊版程式碼的相容性，允許只使用 id, username, experience 三個參數建立物件
   *
   * <p>運作機制:
   *
   * <ol>
   *   <li>接受舊版的 3 參數格式 (id, username, experience)
   *   <li>委派給 canonical constructor，傳入 6 個參數
   *   <li>將傳入的 experience 值同時填入 experience 和 experiencePoints 欄位
   *   <li>將 level 和 role 設為 null (舊版本沒有這些欄位)
   * </ol>
   *
   * <p>為什麼要這樣設計:
   *
   * <ul>
   *   <li>讓舊程式碼可以繼續使用 new UserInfo(id, username, experience)
   *   <li>避免大規模修改現有程式碼
   *   <li>提供平滑的遷移路徑
   *   <li>新舊 API 可以並存，不會互相衝突
   * </ul>
   *
   * <p>範例:
   *
   * <pre>
   * UserInfo user = new UserInfo(1L, "alice", 500);
   * // 結果: id=1, username="alice", experience=500, experiencePoints=500, level=null, role=null
   * </pre>
   *
   * @param id 使用者 ID
   * @param username 使用者名稱
   * @param experience 經驗值 (舊版欄位名稱)
   */
  public UserInfo(Long id, String username, Integer experience) {
    this(id, username, experience, experience, null, null);
  }
}
