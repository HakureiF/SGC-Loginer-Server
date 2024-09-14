package com.seer.seerweb.component;

import java.util.Date;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * The type Delay task.
 *
 */
public class DelayTask implements Delayed {

  private String data;
  private long expire;
  private String gameId;

  /**
   * 构造延时任务.
   *
   * @param data   业务数据
   * @param expire 任务延时时间（ms）
   * @param gameId the game id
   */
  public DelayTask(String data, long expire, String gameId) {
    this.data = data;
    this.expire = expire + System.currentTimeMillis();
    this.gameId = gameId;
  }

  /**
   * Data task base.
   *
   * @return the task base
   */
  public String data() {
    return data;
  }

  /**
   * Game id string.
   *
   * @return the string
   */
  public String gameId() {
    return gameId;
  }


  public void clear() {
    this.data = "";
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof DelayTask) {
      return this.data.equals(((DelayTask) obj).data())
          && this.gameId.equals(((DelayTask) obj).gameId);
    }
    return false;
  }

  @Override
  public String toString() {
    return "{" + "data:" + data + "," + "expire:" + new Date(expire) + "}";
  }

  @Override
  public long getDelay(TimeUnit unit) {
    return unit.convert(this.expire - System.currentTimeMillis(), unit);
  }

  @Override
  public int compareTo(Delayed o) {
    long delta = getDelay(TimeUnit.NANOSECONDS) - o.getDelay(TimeUnit.NANOSECONDS);
    return (int) delta;
  }
}
