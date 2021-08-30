from random import *
def sdes_genkey():
    key = []
    for i in range(0,9):
        key.append(randint(0,1))
    return key
def sdes_compute_function(rblock,roundkey):
    
    return
def sdes_expend(bit):
    j = 0;
    expend_bit = []
    for i in range(0,8):
        if i > 2 and i < 7:
            if i % 2 == 0:
                expend_bit.append(bit[3])
            else:
                expend_bit.append(bit[2])
                j = 4
        else:
            expend_bit.append(bit[j])
            j += 1
        if j > len(bit) - 1:
            break
    return expend_bit

def sdes_sbox(div_bit):
    s0 = {000:111,001:110,010:101,011:000,100:011,101:010,110:100,111:001}
    s1 = {000:001,001:101,010:111,011:110,100:010,101:011,110:000,111,100}
    
    return
def sdes_keyskedule():
    return
def sdes_encrypt(bit,key):
    return
def sdes_decrypt(bit,key):
    return
sdes_genkey()
bit = [0,1,1,0,1,1]
print(len(bit))
print(sdes_expend(bit))
