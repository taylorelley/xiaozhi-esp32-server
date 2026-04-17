package xiaozhi.modules.sys.service;

public interface TokenService {
    /**
     * generatetoken
     *
     * @param userId
     * @return
     */
    String createToken(long userId);
}
