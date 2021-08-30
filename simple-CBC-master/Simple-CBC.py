from random import *
def cbc_genkey():
    key = []
    for i in range(0,9):
        key.append(randint(0,1))
    return key
def list_xor(block1,block2):
    result = []
    for i in range(0,len(block2)):
        result.append(block1[i]^block2[i])
    return result
def sdes_compute_function(rblock,roundkey):
    extend_block = sdes_extend(rblock)
    extend_block = list_xor(extend_block,roundkey)
    result = sdes_sbox(extend_block)
    return result
def sdes_extend(bit):
    j = 0;
    extend_bit = []
    for i in range(0,8):
        if i > 1 and i < 6:
            if i % 2 == 0:
                extend_bit.append(bit[3])
            else:
                extend_bit.append(bit[2])
                j = 4
        else:
            extend_bit.append(bit[j])
            j += 1
        if j == len(bit):
            break
    return extend_bit

def sdes_sbox(block):
    s0_0 = {'000':[1,0,1],'001':[0,1,0],'010':[0,0,1],'011':[1,1,0],'100':[0,1,1],'101':[1,0,0],'110':[1,1,1],'111':[0,0,0]}
    s0_1 = {'000':[0,0,1],'001':[1,0,0],'010':[1,1,0],'011':[0,1,0],'100':[0,0,0],'101':[1,1,1],'110':[1,0,1],'111':[0,1,1]}
    s1_0 = {'000':[1,0,0],'001':[0,0,0],'010':[1,1,0],'011':[1,0,1],'100':[1,1,1],'101':[0,0,1],'110':[0,1,1],'111':[0,1,0]}
    s1_1 = {'000':[1,0,1],'001':[0,1,1],'010':[0,0,0],'011':[1,1,1],'100':[1,1,0],'101':[0,1,0],'110':[0,0,1],'111':[1,0,0]}
    index = ''
    r_block = []
    l_block = []
    l_block.extend(block[0:4])
    r_block.extend(block[4:])
    for i in l_block[1:]:
        index += str(i)
    if l_block[0] == 0:
        trans_bit1 = s0_0[index]
    else:
        trans_bit1 = s0_1[index]
    index = ''
    for i in r_block[1:]:
        index += str(i)
    if r_block[0] == 0:
        trans_bit2 = s1_0[index]
    else:
        trans_bit2 = s1_1[index]
        
    return trans_bit1 + trans_bit2

def sdes_keyskedule(key,roundnum):
    roundkey = []
    n = roundnum
    for i in range(8):
        if n < 9:
            roundkey.append(key[n])
        else:
            n = 0
            roundkey.append(key[n])
        n += 1
    return roundkey

def sdes_encrypt(bit,key,roundlimit = 3):
    rblock = []
    lblock = []
    lblock.extend(bit[0:6])
    rblock.extend(bit[6:])
    for roundnum in range(1,roundlimit+1):
        roundkey = sdes_keyskedule(key,roundnum)
        fblock = sdes_compute_function(rblock,roundkey)
        result = list_xor(lblock,fblock)
        lblock = rblock[:]
        rblock = result[:]
    bit = lblock + rblock
    return bit
def sdes_decrypt(bit,key,roundlimit = 3):
    rblock = []
    lblock = []
    lblock.extend(bit[0:6])
    rblock.extend(bit[6:])
    while roundlimit > 0:
        roundkey = sdes_keyskedule(key,roundlimit)
        fblock = sdes_compute_function(lblock,roundkey)
        result = list_xor(rblock,fblock)
        rblock = lblock[:]
        lblock = result[:]
        roundlimit -= 1
    bit = lblock + rblock
    return bit
def cbc_encrypt(key,iv,plainbit):
    p_len = len(plainbit)
    i = 0
    plain_block = []
    crypto_block = []
    cryptogram = []
    padding = []
    plainbit_num = p_len // 12
    while i < plainbit_num:
        if i == 0:
            plain_block.append(list_xor(iv,plainbit[i*12:i*12+12]))
        else:
            plain_block.append(list_xor(crypto_block[i-1],plainbit[i*12:i*12+12]))
        crypto_block.append(sdes_encrypt(plain_block[i],key))
        cryptogram.extend(crypto_block[i])
        i += 1
    if p_len % 12 != 0:     #plaintext isn't 12 times. add padding
        gap = 12 - p_len % 12
        for j in range(0,gap):
            padding.append(randint(0,1))
        final_plainbit = plainbit[i*12:p_len] + padding
        plain_block.append(list_xor(crypto_block[i-1],final_plainbit))
        cryptogram.extend(sdes_encrypt(plain_block[i],key))
    return cryptogram,len(padding)
def cbc_decrypt(key,iv,cryptogram,padding_num = 0):
    c_len = len(cryptogram)
    i = 0
    plain_block = []
    crypto_block = []
    plainbit = []
    cryptogram_num = c_len / 12
    while i < cryptogram_num:
        crypto_block.append(sdes_decrypt(cryptogram[i*12:i*12+12],key))
        if i == 0:
            plain_block.append(list_xor(iv,crypto_block[i]))
        else:
            plain_block.append(list_xor(cryptogram[(i-1)*12:(i-1)*12+12],crypto_block[i]))
        plainbit.extend(plain_block[i])
        i += 1
    if padding_num != 0: #if padding exists, excepts padding
        plainbit = plainbit[:-padding_num]
    return plainbit
    
        
        
            
    
