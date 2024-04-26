
def pretty_time(seconds: int) -> str:
    h = seconds // 3600
    m = seconds % 3600 // 60
    s = seconds % 3600 % 60
    time_int = []
    time_str = []

    if h > 0:
        time_int.append(h)
    time_int.append(m)
    time_int.append(s)

    for i, val in enumerate(time_int[::-1]):
        if i < len(time_int) - 1 and val < 10:
            val = '0' + str(val)
        time_str.append(str(val))
    return ":".join(time_str[::-1])
